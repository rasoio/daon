package daon.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 분석 결과
 */
public class EojeolInfo {

    private String surface;
    private List<Node> nodes = new ArrayList<>();

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
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
