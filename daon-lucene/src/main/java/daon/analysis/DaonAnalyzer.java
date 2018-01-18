package daon.analysis;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.DecimalDigitFilter;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link Analyzer} for Korean language.
 */
public final class DaonAnalyzer extends Analyzer {

    /**
     * Builds an analyzer
     */
    public DaonAnalyzer() {
    }

    /**
     * Creates
     * {@link TokenStreamComponents}
     * used to tokenize all the text in the provided {@link Reader}.
     *
     * @return {@link TokenStreamComponents}
     * built from a {@link DaonTokenizer} filtered with
     * {@link LowerCaseFilter}, {@link DecimalDigitFilter} and {@link StopFilter}
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new DaonTokenizer();
        TokenStream result = new LowerCaseFilter(source);

        List<String> include = new ArrayList<>();
//        include.add("NNG");
//        include.add("JKB");

        List<String> exclude = new ArrayList<>();
//        exclude.add("SL");

        result = new DaonFilter(result, "index", include, exclude);
//    result = new StopFilter(result, stopwords);
        return new TokenStreamComponents(source, result);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new LowerCaseFilter(in);
        return result;
    }
}
