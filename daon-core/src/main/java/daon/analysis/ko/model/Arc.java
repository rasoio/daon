package daon.analysis.ko.model;

import org.apache.lucene.util.fst.FST;

/**
 * Created by mac on 2017. 6. 9..
 */
public class Arc {
    public State state;
    public FST.Arc<Object> arc;
    public int cnt = 0;


    public Arc(State state, FST.Arc<Object> arc) {
        this.state = state;
        this.arc = arc;
    }

    public Arc(State state, FST.Arc<Object> arc, int cnt) {
        this.state = state;
        this.arc = arc;
        this.cnt = cnt;
    }

    @Override
    public String toString() {
        return "{" +
                "state=" + state +
                ", arc=" + arc +
                ", cnt=" + cnt +
                '}';
    }

    public enum State {
        FOUND, FINAL, NOT_FOUND
    }
}
