package daon.core.processor;

import daon.core.config.MatchType;
import daon.core.handler.EojeolInfoHandler;
import daon.core.result.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ConnectionProcessor {

    private Logger logger = LoggerFactory.getLogger(ConnectionProcessor.class);

    private ModelInfo modelInfo;

    public static ConnectionProcessor create(ModelInfo modelInfo) {

        return new ConnectionProcessor(modelInfo);
    }

    private ConnectionProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    /**
     * 형태소 연결, 최종 result 구성
     * @param lattice lattice
     */
    public void process(Lattice lattice, EojeolInfoHandler handler) {

        if(lattice.getEojeolInfos().size() == 0){
            return;
        }

        Connector connector = Connector.create(modelInfo);

        connect(lattice, connector);

        Node node = reverse(lattice);

        fill(lattice, node, handler);
    }


    private void connect(Lattice lattice, Connector connector){

        int charsLength = lattice.getCharsLength();

        Node[] endNodes = lattice.getEndNodes();

        for(int pos = 0; pos <= charsLength; pos++) {
            Node lnode = endNodes[pos];
            if(lnode != null){
                //띄어쓰기 offset 무시 오른쪽 노드 찾기
                Node rnode = getRightNode(pos, lattice);
                for (;rnode != null; rnode = rnode.getBeginNext()) {

                    //rnode 의 prev node 설정
                    connPrevNode(lnode, rnode, connector);
                }
            }
        }
    }

    private void connPrevNode(Node lnode, Node rnode, Connector connector) {
        int bestCost = 0;
        Node bestNode = null;

        int step = 0;
        for (;lnode != null; lnode = lnode.getEndNext()) {

            //연결 정보가 끊어진 node
            if(lnode.getType() != MatchType.BOS && lnode.getPrev() == null){
                if(logger.isDebugEnabled()) {
                    logger.debug("prev is null lnode : {} : ({}), rnode : {} : ({}), cost : {}", lnode.getSurface(), lnode.getKeywords(), rnode.getSurface(), rnode.getKeywords(), lnode.getBacktraceCost());
                }
                continue;
            }

            int connectionCost = connector.cost(lnode, rnode);
            int cost = lnode.getBacktraceCost() + connectionCost; // cost 값은 누적

            if(logger.isDebugEnabled()) {
                logger.debug("lnode : {} : ({}), rnode : {} : ({}), connectionCost : {} backtraceCost : {}", lnode.getSurface(), lnode.getKeywords(), rnode.getSurface(), rnode.getKeywords(), connectionCost, cost);
            }

            if(step == 0){
                bestCost = cost;
                bestNode  = lnode;
            }else{
                //best 는 left node 중 선택, 즉 prev 설정
                if (cost < bestCost) {
                    bestNode  = lnode;
                    bestCost  = cost;
                }
            }

            step++;
        }

        rnode.setPrev(bestNode);
        rnode.setBacktraceCost(bestCost);
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

    private void fill(Lattice lattice, Node node, EojeolInfoHandler handler) {

        List<EojeolInfo> eojeolInfos = lattice.getEojeolInfos();

        int idx = 0;
        EojeolInfo eojeolInfo = null;
        //BOS, EOS 제외
        for (Node n = node.getNext(); n.getNext() != null; n = n.getNext()){

            if(n.isFirst()){
                eojeolInfo = eojeolInfos.get(idx);
                eojeolInfo.setOffset(n.getOffset());
                idx++;
            }

            if(eojeolInfo != null) {
                eojeolInfo.addNode(n);
            }

            //last node check
            if(MatchType.EOS.equals(n.getNext().getType()) || n.getNext().isFirst()){
                //eojeol 단위 row handler 처리
                handler.handle(eojeolInfo);
            }

            if(logger.isDebugEnabled()) {
                logger.debug("result node : {} : ({},{}) : ({}) : cost : {}, isFirst : {}", n.getSurface(), n.getOffset(), n.getLength(), n.getKeywords(), n.getCost(), n.isFirst());
            }
        }
    }
}
