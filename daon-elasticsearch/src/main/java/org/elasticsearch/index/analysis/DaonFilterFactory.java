package org.elasticsearch.index.analysis;

import daon.analysis.DaonFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class DaonFilterFactory extends AbstractTokenFilterFactory {

    public DaonFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DaonFilter(tokenStream);
    }
}
