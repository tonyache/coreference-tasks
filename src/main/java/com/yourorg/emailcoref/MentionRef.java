package com.yourorg.emailcoref;

/**
 * A single coreference mention produced by CoreNLP, with email-local context.
 */
public record MentionRef(
        String emailId,
        int localCorefClusterId, // CoreNLP's cluster ID (local to that email)
        String text,
        int sentenceIndex,
        int startToken,
        int endToken
) {}
