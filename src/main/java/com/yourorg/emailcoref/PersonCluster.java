package com.yourorg.emailcoref;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A person-level cluster across emails.
 * All mentions and identifiers here are assumed to refer to the same individual.
 */
public class PersonCluster {
    private final String clusterId;          // internal ID
    private final Set<String> emailAddresses = new HashSet<>();
    private final Set<String> names = new HashSet<>();
    private final List<MentionRef> mentions = new ArrayList<>();

    // Optional metadata you can expand later
    private String canonicalName;            // e.g., "Antonio Ache"

    public PersonCluster(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() { return clusterId; }
    public Set<String> getEmailAddresses() { return emailAddresses; }
    public Set<String> getNames() { return names; }
    public List<MentionRef> getMentions() { return mentions; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public void addEmailAddress(String email) {
        if (email != null && !email.isBlank()) {
            emailAddresses.add(email.toLowerCase());
        }
    }

    public void addName(String name) {
        if (name != null && !name.isBlank()) {
            names.add(name.trim());
            if (canonicalName == null) {
                canonicalName = name.trim();
            }
        }
    }

    public void addMention(MentionRef mention) {
        mentions.add(mention);
    }

    @Override
    public String toString() {
        return "PersonCluster{" +
                "clusterId='" + clusterId + '\'' +
                ", emailAddresses=" + emailAddresses +
                ", names=" + names +
                ", mentions=" + mentions.size() +
                '}';
    }
}
