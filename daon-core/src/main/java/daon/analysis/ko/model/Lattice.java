package daon.analysis.ko.model;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.config.MatchType;
import daon.analysis.ko.util.CharTypeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

/**
 * 사전 분석 결과 정보
 *
 */
public class Lattice {
    private Logger logger = LoggerFactory.getLogger(Lattice.class);

    private Node[] startNodes;
    private Node[] endNodes;
    private String[] eojeols;

    private final char[] chars;
    private final int charsLength;

    public Lattice(String sentence) {
        chars = sentence.toCharArray();
        charsLength = chars.length;
        this.eojeols = new String[charsLength + 1];

        this.endNodes = new Node[charsLength + 1];
        this.startNodes = new Node[charsLength + 1];

        Node bos = new Node(0,0, "BOS", MatchType.BOS);

        endNodes[0] = bos;
    }

    public void add(Node node){

        int start = node.getOffset();
        int length = node.getLength();
        int end = start + length;


//       >= start && < end => endNodes[i] = null;
//        > start && <= end => startNodes[i] = null;

        if(node.isMatchAll()) {
            IntStream.range(start, end).forEach(i -> {
                if(i > 0) {
                    endNodes[i] = null;
                }
            });

            IntStream.rangeClosed(start + 1, end).forEach(i -> {
                startNodes[i] = null;
            });
        }

        addStartNode(node);
        addEndNode(node);
    }

    public void addEndNode(Node node){

        int pos = node.getOffset();
        int endIdx = node.getLength() + pos;
        Node e = endNodes[endIdx];
        node.setEndNext(e);

        endNodes[endIdx] = node;
    }

    public void addStartNode(Node node){

        int pos = node.getOffset();
        Node s = startNodes[pos];
        node.setBeginNext(s);

        startNodes[pos] = node;
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
