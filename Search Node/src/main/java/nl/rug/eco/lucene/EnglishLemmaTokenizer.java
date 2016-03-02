
package nl.rug.eco.lucene;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import com.google.common.collect.Iterables;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class EnglishLemmaTokenizer extends TokenStream {
    private Iterator<TaggedWord> tagged;
    private PositionIncrementAttribute posIncr;
    private TaggedWord currentWord;
    private TermAttribute termAtt;
    private boolean lemmaNext;

    public EnglishLemmaTokenizer(Reader input, String posModelFile)
            throws Exception {
        this(input, EnglishLemmaAnalyzer.makeTagger(posModelFile));
    }
    
    public EnglishLemmaTokenizer(Reader input, MaxentTagger tagger) {
        super();

        lemmaNext = false;
        posIncr = addAttribute(PositionIncrementAttribute.class);
        termAtt = addAttribute(TermAttribute.class);

        List<List<HasWord>> tokenized =
            MaxentTagger.tokenizeText(input);
        tagged = Iterables.concat(tagger.process(tokenized)).iterator();
    }


    @Override
    public final boolean incrementToken() throws IOException {
        if (lemmaNext) {
           posIncr.setPositionIncrement(1);
            String tag  = currentWord.tag();
            String form = currentWord.word();
            termAtt.setTermBuffer(Morphology.stemStatic(form, tag).word());
        } else {
     
            int increment = 0;
            for (;;) {
                if (!tagged.hasNext())
                    return false;
                currentWord = tagged.next();
                if (!unwantedPOS(currentWord.tag()))
                {
                	//System.out.print(currentWord.toString());
                	//System.out.println("Unwanted ");
                    break;}
                increment++;
            }
            posIncr.setPositionIncrement(increment);
            termAtt.setTermBuffer(currentWord.word());
        }

        lemmaNext = !lemmaNext;
        return true;
    }

    private static final Pattern unwantedPosRE = Pattern.compile(
      "^(CC|DT|[LR]RB|MD|POS|PRP|UH|WDT|WP|WP\\$|WRB|\\$|\\#|\\.|\\,|:)$"
    	
    );
    /**
     * Determines if words with a given POS tag should be omitted from the
     * index. Defaults to filtering out punctuation and function words
     * (pronouns, prepositions, "the", "a", etc.).
     */
    protected boolean unwantedPOS(String tag) {
        return unwantedPosRE.matcher(tag).matches();
    }
}