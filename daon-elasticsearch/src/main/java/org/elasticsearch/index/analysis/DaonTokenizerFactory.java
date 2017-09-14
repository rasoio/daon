package org.elasticsearch.index.analysis;

import daon.analysis.DaonTokenizer;
import daon.core.reader.ModelReader;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaonTokenizerFactory  extends AbstractTokenizerFactory {

    private Logger logger = LoggerFactory.getLogger(DaonTokenizerFactory.class);

    public DaonTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public Tokenizer create() {
        DaonTokenizer tokenizer = new DaonTokenizer();
        return tokenizer;
    }

}
