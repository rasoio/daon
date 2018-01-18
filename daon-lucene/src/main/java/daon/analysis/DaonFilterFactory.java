package daon.analysis;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DaonFilterFactory extends TokenFilterFactory {

    /**
     * type : index, query
     * index : eojeol suface, node suface, keyword word
     * query : keyword word
     *
     * includeTag
     * Nouns, Verbs, 관형사, 부사, 감탄사, Josa, Eomi, Number, ETC
     *
     * excludeTag
     * Josa, Eomi
     */
    private final String mode;
    private final List<String> include;
    private final List<String> exclude;

    public DaonFilterFactory(Map<String, String> args) {
        super(args);

        mode = get(args, "mode", "index");
        Set<String> includeSet = getSet(args, "include");
        Set<String> excludeSet = getSet(args, "exclude");

        if(includeSet != null){
            include = new ArrayList<>(includeSet);
        }else{
            include = null;
        }

        if(excludeSet != null){
            exclude = new ArrayList<>(excludeSet);
        }else{
            exclude = null;
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new DaonFilter(input, mode, include, exclude);
    }
}
