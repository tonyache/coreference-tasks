package com.yourorg.emailcoref;

import java.text.Normalizer;
import java.util.*;

/**
 * Builds person-level clusters across a collection of emails using:
 *  - header info (From: name + email)
 *  - CoreNLP mentions (pronouns, names, etc.)
 *
 * This is intentionally simple and heuristic-based; you can refine it as needed.
 */
public class IdentifierClusterer {

    private final Map<String, PersonCluster> clustersByEmail = new HashMap<>();
    private final Map<String, PersonCluster> clustersByNameKey = new HashMap<>();
    private int nextClusterId = 1;

    /**
     * Main entry point: produce person clusters from a set of emails with mentions.
     */
    public Collection<PersonCluster> cluster(List<EmailWithMentions> emails) {
        // 1) Ensure sender of each email has a cluster
        for (EmailWithMentions ewm : emails) {
            EmailMessage email = ewm.email();
            ensureSenderCluster(email);
        }

        // 2) Assign mentions to clusters (or create name-based clusters if needed)
        for (EmailWithMentions ewm : emails) {
            assignMentions(ewm);
        }

        // 3) Optionally: merge clusters that share name keys but no email yet (simple pass)
        mergeNameOnlyClusters();

        return new ArrayList<>(clustersByEmail.values());
    }

    private void ensureSenderCluster(EmailMessage email) {
        String fromEmail = normalizeEmail(email.getFromEmail());
        String fromName = email.getFromName();

        if (fromEmail == null && (fromName == null || fromName.isBlank())) {
            return; // nothing to do
        }

        PersonCluster cluster = null;

        if (fromEmail != null) {
            cluster = clustersByEmail.get(fromEmail);
        }

        if (cluster == null && fromName != null && !fromName.isBlank()) {
            String key = normalizedNameKey(fromName);
            cluster = clustersByNameKey.get(key);
        }

        if (cluster == null) {
            cluster = createNewCluster();
        }

        if (fromEmail != null) {
            clustersByEmail.put(fromEmail, cluster);
            cluster.addEmailAddress(fromEmail);
        }

        if (fromName != null && !fromName.isBlank()) {
            String key = normalizedNameKey(fromName);
            clustersByNameKey.putIfAbsent(key, cluster);
            cluster.addName(fromName);
        }
    }

    private void assignMentions(EmailWithMentions ewm) {
        EmailMessage email = ewm.email();
        List<MentionRef> mentions = ewm.mentions();

        String senderEmail = normalizeEmail(email.getFromEmail());
        String senderName = email.getFromName();
        PersonCluster senderCluster = senderEmail != null
                ? clustersByEmail.get(senderEmail)
                : null;

        for (MentionRef mention : mentions) {
            String text = mention.text();
            if (text == null || text.isBlank()) {
                continue;
            }

            PersonCluster cluster = null;

            // 1) Does it clearly look like the sender?
            if (senderCluster != null && nameMatches(text, senderName)) {
                cluster = senderCluster;
            }

            // 2) Otherwise, try match against known names in existing clusters
            if (cluster == null) {
                cluster = findClusterByMentionName(text);
            }

            // 3) If still no cluster, create a "name-only" cluster for this mention
            if (cluster == null) {
                cluster = createNewCluster();
                String key = normalizedNameKey(text);
                clustersByNameKey.putIfAbsent(key, cluster);
                cluster.addName(text);
            }

            cluster.addMention(mention);
        }
    }

    /**
     * Try to find a cluster whose known names match this mention text.
     */
    private PersonCluster findClusterByMentionName(String mentionText) {
        String mentionKey = normalizedNameKey(mentionText);

        // Direct name-key hit
        PersonCluster cluster = clustersByNameKey.get(mentionKey);
        if (cluster != null) {
            return cluster;
        }

        // Fuzzy match against existing cluster names (simple heuristics)
        for (PersonCluster c : clustersByEmail.values()) {
            for (String name : c.getNames()) {
                if (nameMatches(mentionText, name)) {
                    return c;
                }
            }
        }

        // Also consider name-only clusters (not yet tied to any email)
        for (PersonCluster c : clustersByNameKey.values()) {
            for (String name : c.getNames()) {
                if (nameMatches(mentionText, name)) {
                    return c;
                }
            }
        }

        return null;
    }

    /**
     * Simple merging pass:
     * if two name-only clusters share the same normalized name key, merge them.
     * (This is conservative but illustrates the idea.)
     */
    private void mergeNameOnlyClusters() {
        // For now this is a placeholder / no-op; you can implement more aggressive merging if needed.
        // Example idea:
        //  - create a map from normalized name key to a single "canonical" cluster
        //  - redirect all others into it, reattaching mentions and names.
    }

    private PersonCluster createNewCluster() {
        String id = "P" + nextClusterId++;
        return new PersonCluster(id);
    }

    // ------------- Normalization & matching helpers ----------------

    private static String normalizeEmail(String email) {
        if (email == null) return null;
        email = email.trim();
        if (email.isEmpty()) return null;
        return email.toLowerCase();
    }

    /**
     * Normalize a name into a key suitable for equality / map lookups.
     *  - lower case
     *  - strip accents
     *  - strip titles like "dr.", "mr.", "ms."
     *  - remove punctuation
     *  - collapse whitespace
     */
    private static String normalizedNameKey(String raw) {
        if (raw == null) return "";

        String s = raw.trim().toLowerCase(Locale.ROOT);

        // Strip accents
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        // Remove common titles
        s = s.replaceAll("\\b(dr|mr|mrs|ms|prof)\\.?\\s*", "");

        // Remove punctuation
        s = s.replaceAll("[^a-z0-9\\s]", " ");

        // Collapse whitespace
        s = s.replaceAll("\\s+", " ").trim();

        return s;
    }

    /**
     * Heuristic: does mentionText likely refer to the same person as fullName?
     * Rules (very simple):
     *  - exact normalized name key match, OR
     *  - mention is a single token and is contained in the full normalized name, OR
     *  - first token matches first name of fullName.
     */
    private static boolean nameMatches(String mentionText, String fullName) {
        if (mentionText == null || fullName == null) return false;

        String mentionKey = normalizedNameKey(mentionText);
        String fullKey = normalizedNameKey(fullName);

        if (mentionKey.isEmpty() || fullKey.isEmpty()) return false;

        if (mentionKey.equals(fullKey)) {
            return true;
        }

        String[] mentionTokens = mentionKey.split(" ");
        String[] fullTokens = fullKey.split(" ");

        // Single token contained in full name: "tony" in "antonio ache"
        if (mentionTokens.length == 1 && fullKey.contains(mentionKey)) {
            return true;
        }

        // First token match: "antonio" vs "antonio ache"
        if (mentionTokens.length == 1 && fullTokens.length > 0 &&
            mentionTokens[0].equals(fullTokens[0]) &&
            mentionTokens[0].length() >= 3) {
            return true;
        }

        return false;
    }
}
