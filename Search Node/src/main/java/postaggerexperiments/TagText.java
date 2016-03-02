package postaggerexperiments;

import java.io.IOException;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
 
public class TagText {
    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
 
        // Initialize the tagger
        MaxentTagger tagger = new MaxentTagger(
                "/home/sanket/Downloads/stanford-postagger-2014-10-26/models/english-bidirectional-distsim.tagger");
 
        // The sample string
        String sample = "She threw up her dinner";
 
        // The tagged string
        String tagged = tagger.tagString(sample);
 
        // Output the result
        System.out.println(tagged);
    }
}