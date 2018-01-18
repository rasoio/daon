package org.elasticsearch.index.analysis;

import daon.analysis.DaonFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.util.List;
import java.util.Set;

public class DaonFilterFactory extends AbstractTokenFilterFactory {

    private final String mode;
    private final List<String> include;
    private final List<String> exclude;

    public DaonFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);

        mode = settings.get("mode", "index");

        include = settings.getAsList("include");
        exclude = settings.getAsList("exclude");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DaonFilter(tokenStream, mode, include, exclude);
    }
}
