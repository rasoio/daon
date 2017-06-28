package daon.analysis.ko.processor;

import daon.analysis.ko.model.Arc;
import org.apache.lucene.util.fst.FST;

import java.io.IOException;

/**
 * Created by mac on 2017. 6. 27..
 */
public class ConnectionFinder {
    private FST<Long> fst;
    private FST.BytesReader fstReader;

    public ConnectionFinder(FST<Long> fst) {

        this.fst = fst;
        fstReader = fst.getBytesReader();
    }

    public Arc initArc(){

        return new Arc(Arc.State.NOT_FOUND, fst.getFirstArc(new FST.Arc<>()));
    }

    // next 가 return
    public Arc find(int seq, Arc followArc){

        // next 값
        FST.Arc next = new FST.Arc<>();

        FST.Arc<Long> follow = followArc.arc;
        int cnt = followArc.cnt;

        Arc.State state = Arc.State.NOT_FOUND;

        //탐색 결과 있을때
        try {
            if (fst.findTargetArc(seq, follow, next, fstReader) != null) {
                state = Arc.State.FOUND;
                cnt++;
            }else{
                cnt = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
//            state = State.NOT_FOUND;
        }

        // 매핑 종료
        if (next.isFinal()) {
            state = Arc.State.FINAL;
        }

        return new Arc(state, next, cnt);
    }

}
