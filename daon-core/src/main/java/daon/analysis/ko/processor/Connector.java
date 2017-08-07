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
//        float conn = calculateConn();
        int tagTrans = calculateTagTrans(lnode, rnode);

        //tagTrans score

        int wcost = rnode.getWcost();

//        matrix_[lNode->rcAttr + lsize_ * rNode->lcAttr] +
//                rNode->wcost +
//                        get_space_penalty_cost(rNode);


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
        // lnode == bos then firstTagScore(rnode)
        // rnode == eos then lastTagScore(lnode)
        // lnode == first && rnode != first then firstTagScore(lnode) + middleTagTransScore(lnode, rnode);
        // lnode != first && rnode == first then lastTagScore(lnode) + connTagTransScore(lnode, rnode);
        // lnode == first && rnode == first then firstTagScore(lnode) + connTagTransScore(lnode, rnode);
        // lnode != first && rnode != first then middleTagTransScore(lnode, rnode);

        return score;
    }

    private int firstTagTransScore(POSTag t){
        return modelInfo.getTagScore(POSTag.FIRST.name(), t.name());
    }

    private int lastTagTransScore(POSTag t){
        return modelInfo.getTagScore(t.name(), POSTag.LAST.name());
    }

    private int middleTagTransScore(POSTag t1, POSTag t2){
        return modelInfo.getTagScore(t1.name(), t2.name());
    }

    private int connTagTransScore(POSTag t1, POSTag t2){
        String t = t1.name() + "|END";

        return modelInfo.getTagScore(t, t2.name());
    }


}
