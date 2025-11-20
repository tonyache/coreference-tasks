package com.yourorg.emailcoref;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CorefResolver {

    private final StanfordCoreNLP pipeline;

    public CorefResolver(StanfordCoreNLP pipeline) {
        this.pipeline = pipeline;
    }

    public List<MentionRef> resolve(EmailMessage email) {
        Annotation doc = new Annotation(email.getBody());
        pipeline.annotate(doc);

        Map<Integer, CorefChain> chains =
                doc.get(CorefCoreAnnotations.CorefChainAnnotation.class);

        List<MentionRef> results = new ArrayList<>();

        if (chains == null) return results;

        for (Map.Entry<Integer, CorefChain> entry : chains.entrySet()) {
            int clusterId = entry.getKey();
            CorefChain chain = entry.getValue();

            chain.getMentionsInTextualOrder().forEach(m -> {
                results.add(
                    new MentionRef(
                        email.getMessageId(),
                        clusterId,
                        m.mentionSpan,
                        m.sentNum - 1,
                        m.startIndex - 1,
                        m.endIndex - 1
                    )
                );
            });
        }

        return results;
    }
}
