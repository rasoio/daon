package daon.analysis.ko.processor;

import daon.analysis.ko.config.MatchType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by mac on 2017. 5. 18..
 */
public class Connector {

    private Logger logger = LoggerFactory.getLogger(Connector.class);

    private static final int MAX_SCORE = 5000;

    private ModelInfo modelInfo;

    public static Connector create(ModelInfo modelInfo) {

        return new Connector(modelInfo);
    }

    private Connector(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }


    /**
     * cost
     */
    public int cost(Node lnode, Node rnode) {

        int cost = 0;

        //tagTrans score
        int tagTrans = calculateTagTrans(lnode, rnode);

        //word cost score
        int wcost = rnode.getWcost();

        cost = tagTrans + wcost;

        return cost;
    }


    private int calculateTagTrans(Node lnode, Node rnode) {

        int score = 0;

        if(lnode.getType() == MatchType.BOS){
            score = firstTagTransScore(rnode.getFirst().getTag());
        }else if(rnode.getType() == MatchType.EOS){
//            score = lastTagTransScore(lnode.getLast().getTag());
        }else if(lnode.isFirst() && !rnode.isFirst()){
            score = (firstTagTransScore(lnode.getFirst().getTag()) + middleTagTransScore(lnode.getLast().getTag(), rnode.getFirst().getTag())) / 2;
        }else if(!lnode.isFirst() && rnode.isFirst()){
            score = (lastTagTransScore(lnode.getLast().getTag()) + connTagTransScore(lnode.getLast().getTag(), rnode.getFirst().getTag())) / 2;
        }else if(lnode.isFirst() && rnode.isFirst()){
            score = (firstTagTransScore(lnode.getFirst().getTag()) + connTagTransScore(lnode.getLast().getTag(), rnode.getFirst().getTag())) / 2;
        }else if(!lnode.isFirst() && !rnode.isFirst()){
            score = middleTagTransScore(lnode.getLast().getTag(), rnode.getFirst().getTag());
        }

        return score;
    }

    private int firstTagTransScore(POSTag t){

        Integer score = modelInfo.getFirstTags()[t.getIdx()];

        if(score == null){
            return MAX_SCORE;
        }

        return score;
    }

    private int lastTagTransScore(POSTag t){
        Integer score = modelInfo.getLastTags()[t.getIdx()];

        if(score == null){
            return MAX_SCORE;
        }

        return score;
    }

    private int middleTagTransScore(POSTag t1, POSTag t2){
        Integer score = modelInfo.getMiddleTags()[t1.getIdx()][t2.getIdx()];

        if(score == null){
            return MAX_SCORE;
        }

        return score;
    }

    private int connTagTransScore(POSTag t1, POSTag t2){
        Integer score = modelInfo.getConnectTags()[t1.getIdx()][t2.getIdx()];

        if(score == null){
            return MAX_SCORE;
        }

        return score;
    }


}
