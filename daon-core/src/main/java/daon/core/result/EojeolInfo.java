package daon.core.result;

import java.util.ArrayList;
import java.util.List;

/**
 * 분석 결과
 */
public class EojeolInfo {

    private int seq;

    private String surface;

    private int offset;

    private List<Node> nodes = new ArrayList<>();

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void addNode(Node node){
        nodes.add(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {

        return "{" +
                "surface='" + surface + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}
