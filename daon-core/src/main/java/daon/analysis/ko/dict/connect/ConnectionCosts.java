package daon.analysis.ko.dict.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.POSTag;

public class ConnectionCosts {

    private Logger logger = LoggerFactory.getLogger(ConnectionCosts.class);

    /**
     * 메인 태그 확률
     */
    float rootProb[];

    /**
     * 연결 태그 확률
     */
    float connProb[][];

    public ConnectionCosts(float[] rootProb, float[][] connProb) {
        this.rootProb = rootProb;
        this.connProb = connProb;
    }

    public float score(POSTag prevTag, POSTag curTag) {

        float score = connProb[prevTag.getIdx()][curTag.getIdx()];

        return score;
    }

    public float score(POSTag tag) {

        float score = rootProb[tag.getIdx()];

        return score;
    }
}
