package daon.core.processor;

import daon.core.config.MatchType;
import daon.core.config.POSTag;
import daon.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 연결 cost 계산
 */
public class Connector {

    private Logger logger = LoggerFactory.getLogger(Connector.class);

    /**
     * cost 정보가 없는 경우 초기 cost
     */
    private static final int MAX_COST = 5000;

    private ModelInfo modelInfo;

    public static Connector create(ModelInfo modelInfo) {

        return new Connector(modelInfo);
    }

    private Connector(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }


    /**
     * cost calculate
     * @param lnode left node
     * @param rnode right node
     * @return cost value
     */
    public int cost(Node lnode, Node rnode) {

        int cost = 0;

        //tagTrans cost
        int tagTransCost = calculateTagTrans(lnode, rnode);

        //rnode cost cost
        int rnodeCost = rnode.getCost();

        cost = tagTransCost + rnodeCost;

        return cost;
    }


    private int calculateTagTrans(Node lnode, Node rnode) {

        int cost = 0;

        if(lnode.getType() == MatchType.BOS){
            cost = firstTagTransCost(rnode.getFirst().getTag());
        }else if(rnode.getType() == MatchType.EOS){
            cost = 0;
        }else if(lnode.isFirst() && !rnode.isFirst()){
            cost = (firstTagTransCost(lnode.getFirst().getTag()) + middleTagTransCost(lnode.getLast().getTag(), rnode.getFirst().getTag())) / 2;
        }else if(!lnode.isFirst() && rnode.isFirst()){
            cost = (lastTagTransCost(lnode.getLast().getTag()) + connTagTransCost(lnode.getLast().getTag(), rnode.getFirst().getTag())) / 2;
        }else if(lnode.isFirst() && rnode.isFirst()){
            cost = (firstTagTransCost(lnode.getFirst().getTag()) + connTagTransCost(lnode.getLast().getTag(), rnode.getFirst().getTag())) / 2;
        }else if(!lnode.isFirst() && !rnode.isFirst()){
            cost = middleTagTransCost(lnode.getLast().getTag(), rnode.getFirst().getTag());
        }

        return cost;
    }

    private int firstTagTransCost(POSTag t){

        Integer cost = modelInfo.getFirstTags()[t.getIdx()];

        return defaultIfNull(cost);
    }

    private int lastTagTransCost(POSTag t){
        Integer cost = modelInfo.getLastTags()[t.getIdx()];

        return defaultIfNull(cost);
    }

    private int middleTagTransCost(POSTag t1, POSTag t2){
        Integer cost = modelInfo.getMiddleTags()[t1.getIdx()][t2.getIdx()];

        return defaultIfNull(cost);
    }

    private int connTagTransCost(POSTag t1, POSTag t2){
        Integer cost = modelInfo.getConnectTags()[t1.getIdx()][t2.getIdx()];

        return defaultIfNull(cost);
    }

    private int defaultIfNull(Integer cost){
        if(cost == null){
            return MAX_COST;
        }

        return cost;
    }


}
