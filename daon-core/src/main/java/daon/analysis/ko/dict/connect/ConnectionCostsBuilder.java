package daon.analysis.ko.dict.connect;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.reader.Reader;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.TagCost;

public class ConnectionCostsBuilder {

    private Logger logger = LoggerFactory.getLogger(ConnectionCostsBuilder.class);

    private Config config = new Config();
    private Reader<TagConnection> reader;

    public static ConnectionCostsBuilder create() {
        return new ConnectionCostsBuilder();
    }

    private ConnectionCostsBuilder() {
    }

    public final ConnectionCostsBuilder setFileName(final String fileName) {
        this.config.define(Config.FILE_NAME, fileName);
        return this;
    }

    public final ConnectionCostsBuilder setReader(final Reader<TagConnection> reader) {
        this.reader = reader;
        return this;
    }

    public final ConnectionCostsBuilder setValueType(final Class<TagConnection> valueType) {
        this.config.define(Config.VALUE_TYPE, valueType);
        return this;
    }

    public ConnectionCosts build() throws IOException {

        if (reader == null) {
            //TODO throw exception
        }

        try {
            reader.read(config);

            //최대 사이즈
            int size = Config.POSTag.fin.getIdx() + 1;

            float connProb[][] = new float[size][size];
            float rootProb[] = new float[size];

            //초기화
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    connProb[i][j] = Config.MISS_PENALTY_SCORE;
                }

                rootProb[i] = Config.MISS_PENALTY_SCORE;
            }

            while (reader.hasNext()) {
                TagConnection tag = reader.next();

                String mainTagName = tag.getTag();

                for (TagCost subTag : tag.getTags()) {

                    POSTag subPosTag = subTag.getTag();

                    if ("Root".equals(mainTagName)) {
                        rootProb[subPosTag.getIdx()] = subTag.getProb();

                    } else {
                        POSTag mainPosTag = POSTag.valueOf(mainTagName);
                        connProb[mainPosTag.getIdx()][subPosTag.getIdx()] = subTag.getProb();
                    }
                }
            }

            return new ConnectionCosts(rootProb, connProb);
        } finally {
            reader.close();
        }
    }
}
