package com.yourorg.emailcoref;

import java.util.List;

/**
 * Bundles an email with its CoreNLP mentions.
 */
public record EmailWithMentions(
        EmailMessage email,
        List<MentionRef> mentions
) {}
