package daon.analysis.ko.model;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.config.MatchType;
import daon.analysis.ko.util.CharTypeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 사전 분석 결과 정보
 */
public class Lattice {
    private Logger logger = LoggerFactory.getLogger(Lattice.class);

    private Node[] startNodes;
    private Node[] endNodes;

    private final char[] chars;
    private final int charsLength;

    private List<EojeolInfo> eojeolInfos = new ArrayList<>();

    public Lattice(String sentence) {
        chars = sentence.toCharArray();
        charsLength = chars.length;

        this.endNodes = new Node[charsLength + 1];
        this.startNodes = new Node[charsLength + 1];

        Node bos = createBOS();
        Node eos = createEOS();

        endNodes[0] = bos;
        startNodes[charsLength] = eos;
    }


    private Node createBOS(){
        return new Node(0,0, MatchType.BOS.name(), MatchType.BOS);
    }

    private Node createEOS(){
        return new Node(0,0, MatchType.EOS.name(), MatchType.EOS);
    }

    public void add(Node node){

        int start = node.getOffset();
        int length = node.getLength();
        int end = start + length;


//       >= start && < end => endNodes[i] = null;
//        > start && <= end => startNodes[i] = null;

//        if(node.isMatchAll()) {
//            IntStream.range(start, end).forEach(i -> {
//                if(i > 0) {
//                    endNodes[i] = null;
//                }
//            });
//
//            IntStream.rangeClosed(start + 1, end).forEach(i -> {
//                startNodes[i] = null;
//            });
//        }

        addStartNode(node);
        addEndNode(node);
    }

    private void addEndNode(Node node){

        int pos = node.getOffset();
        int endIdx = node.getLength() + pos;
        Node e = endNodes[endIdx];
        node.setEndNext(e);

        endNodes[endIdx] = node;
    }

    private void addStartNode(Node node){

        int pos = node.getOffset();
        Node s = startNodes[pos];
        node.setBeginNext(s);

        startNodes[pos] = node;
    }

    public List<EojeolInfo> getEojeolInfos() {
        return eojeolInfos;
    }

    public char[] getChars() {
        return chars;
    }

    public int getCharsLength() {
        return charsLength;
    }

    public Node[] getStartNodes() {
        return startNodes;
    }

    public Node[] getEndNodes() {
        return endNodes;
    }

    public Node getStartNode(int pos) {
        return startNodes[pos];
    }


}
