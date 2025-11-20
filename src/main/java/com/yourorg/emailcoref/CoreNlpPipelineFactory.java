package com.yourorg.emailcoref;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.Properties;

public class CoreNlpPipelineFactory {

    public static StanfordCoreNLP createEnglishCorefPipeline() {
        Properties props = new Properties();

        // Order matters: coref depends on parse, ner, etc.
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");

        // Use neural coref
        props.setProperty("coref.algorithm", "neural");
        props.setProperty("coref.language", "en");

        return new StanfordCoreNLP(props);
    }
}
