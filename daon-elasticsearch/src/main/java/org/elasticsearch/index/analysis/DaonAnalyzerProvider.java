package org.elasticsearch.index.analysis;

import daon.analysis.DaonAnalyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class DaonAnalyzerProvider extends AbstractIndexAnalyzerProvider<DaonAnalyzer> {

    private final DaonAnalyzer analyzer;

    public DaonAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);

        analyzer = new DaonAnalyzer();
    }

    @Override
    public DaonAnalyzer get() {
        return analyzer;
    }


}
