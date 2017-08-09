package daon.analysis.ko.processor;

import daon.analysis.ko.config.MatchType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.*;
import daon.analysis.ko.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ConnectionProcessor {

    private Logger logger = LoggerFactory.getLogger(ConnectionProcessor.class);

    private final static int MAX_COST = 1000000;

    private ModelInfo modelInfo;

    public static ConnectionProcessor create(ModelInfo modelInfo) {

        return new ConnectionProcessor(modelInfo);
    }

    private ConnectionProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    /**
     * 형태소 연결, 최종 result 구성
     * @param lattice
     */
    public void process(Lattice lattice) {
        Connector connector = Connector.create(modelInfo);

        connect(lattice, connector);

        Node node = reverse(lattice);

        fillEojeolInfos(lattice, node);
    }


    private void connect(Lattice lattice, Connector connector){

        int charsLength = lattice.getCharsLength();

        Node[] endNodes = lattice.getEndNodes();

        for(int pos = 0; pos <= charsLength; pos++) {
            if(endNodes[pos] != null){

                Node rnode = getRightNode(pos, lattice);

//                int skipPos = 0;

//                if(rnode != null && rnode.isMatchAll()){
//                    skipPos = rnode.getLength() - 1;
//                }

                for (;rnode != null; rnode = rnode.getBeginNext()) {

                    Node lnode = endNodes[pos];

                    setPrevNode(lnode, rnode, connector);
                }

//                pos += skipPos;
            }
        }
    }

    private void setPrevNode(Node lnode, Node rnode, Connector connector) {
        int best_cost = MAX_COST;
        Node best_node = null;

        for (;lnode != null; lnode = lnode.getEndNext()) {

            if(lnode.getType() != MatchType.BOS && lnode.getPrev() == null){
                logger.debug("prev is null lnode : {} : ({}), rnode : {} : ({}), cost : {}", lnode.getSurface(), lnode.getKeywords(), rnode.getSurface(), rnode.getKeywords(), lnode.getCost());
                continue;
            }

            int lcost = connector.cost(lnode, rnode);
            int cost = lnode.getCost() + lcost; // cost 값은 누적

            logger.debug("lnode : {} : {} : ({}), rnode : {} : ({}), cost : {}", lnode.getSurface(), lnode.getCost(), lnode.getKeywords(), rnode.getSurface(), rnode.getKeywords(), cost);

            //best 는 left node 중 선택, 즉 prev 설정
            if (cost < best_cost) {
                best_node  = lnode;
                best_cost  = cost;
            }

        }

        rnode.setPrev(best_node);
        rnode.setCost(best_cost);
    }

    private Node getRightNode(int offset, Lattice lattice){
        for(int pos = offset; pos<= lattice.getCharsLength(); pos++) {
            Node node = lattice.getStartNode(pos);

            if(node != null){
                return node;
            }
        }

        return null;
    }

    private Node reverse(Lattice lattice) {
        int charsLength = lattice.getCharsLength();
        Node node = lattice.getStartNode(charsLength);

        for (Node prevNode; node.getPrev() != null;) {
            prevNode = node.getPrev();
            prevNode.setNext(node);
            node = prevNode;
        }

        return node;
    }

    private void fillEojeolInfos(Lattice lattice, Node node) {

        List<EojeolInfo> eojeolInfos = lattice.getEojeolInfos();

        int idx = 0;
        EojeolInfo eojeolInfo = null;
        //BOS, EOS 제외
        for (Node n = node.getNext(); n.getNext() != null; n = n.getNext()){

            if(n.isFirst()){
                eojeolInfo = eojeolInfos.get(idx);
                idx++;
            }

            if(eojeolInfo != null) {
                eojeolInfo.addNode(n);
            }

            logger.debug("result node : {} : ({},{}) : ({}) : cost : {}, isFirst : {}", n.getSurface(), n.getOffset(), n.getLength(), n.getKeywords(), n.getCost(), n.isFirst());
        }
    }




}
