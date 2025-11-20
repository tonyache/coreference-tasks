package com.yourorg.emailcoref;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DemoMain {

    public static void main(String[] args) {
        // 1) Build CoreNLP pipeline once
        StanfordCoreNLP pipeline = CoreNlpPipelineFactory.createEnglishCorefPipeline();
        CorefResolver corefResolver = new CorefResolver(pipeline);

        // 2) Load your emails (from files, DB, or a hardcoded demo)
        List<EmailMessage> emails = loadEmailsSomehow();

        // 3) Run coref per email
        List<EmailWithMentions> emailWithMentions = new ArrayList<>();
        for (EmailMessage email : emails) {
            List<MentionRef> mentions = corefResolver.resolve(email);
            emailWithMentions.add(new EmailWithMentions(email, mentions));

            // Optional: print mentions for debugging
            System.out.println("==== Mentions for email " + email.getMessageId()
                    + " (" + email.getFromName() + ") ====");
            mentions.forEach(System.out::println);
            System.out.println();
        }

        // 4) Cluster across emails
        IdentifierClusterer clusterer = new IdentifierClusterer();
        Collection<PersonCluster> personClusters = clusterer.cluster(emailWithMentions);

        // 5) Inspect / export
        System.out.println("==== Person clusters ====");
        for (PersonCluster pc : personClusters) {
            System.out.println(pc.getClusterId() + ":");
            System.out.println("  canonicalName: " + pc.getCanonicalName());
            System.out.println("  emails: " + pc.getEmailAddresses());
            System.out.println("  names: " + pc.getNames());
            System.out.println("  #mentions: " + pc.getMentions().size());
            System.out.println();
        }
    }

    /**
     * For now this can just return the fake thread we built earlier.
     * Later you can replace this with code that reads from files / DB.
     */
    private static List<EmailMessage> loadEmailsSomehow() {
        List<EmailMessage> emails = new ArrayList<>();

        emails.add(new EmailMessage(
                "m1",
                "thread-1",
                "Antonio Ache",
                "antonio@example.com",
                "Draft review",
                """
                Hi John,

                I looked at the draft you sent yesterday. Antonio added a few comments.
                Tony will finalize the introduction section tonight if he has time.

                Best,
                Antonio
                """
        ));

        emails.add(new EmailMessage(
                "m2",
                "thread-1",
                "John Smith",
                "jsmith@corp.com",
                "Re: Draft review",
                """
                Hi Antonio,

                Thanks for reviewing the document. Tony's suggestions on the introduction
                were very helpful. John will update the figures and he will share a new
                version tomorrow.

                Best,
                John
                """
        ));

        emails.add(new EmailMessage(
                "m3",
                "thread-1",
                "Tony",
                "tony@example.com",
                "Re: Draft review",
                """
                Hi John,

                I made one more pass on the introduction. Antonio also fixed a typo
                that he found in the abstract.

                Cheers,
                Tony
                """
        ));

        return emails;
    }
}
