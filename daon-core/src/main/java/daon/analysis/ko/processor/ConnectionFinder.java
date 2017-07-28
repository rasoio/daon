package daon.analysis.ko.processor;

import daon.analysis.ko.model.Arc;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.model.Term;
import org.apache.lucene.util.fst.FST;

import java.io.IOException;

/**
 * Created by mac on 2017. 6. 27..
 */
public class ConnectionFinder {
    private FST<Long> connfst;
    private FST.BytesReader fstReader;
    private FST<Long> innerFst;
//    private FST<Long> outerFst;

    public ConnectionFinder(ModelInfo modelInfo) {

        connfst = modelInfo.getConnFst();
        fstReader = connfst.getBytesReader();

        innerFst = modelInfo.getInnerFst();
//        outerFst = modelInfo.getOuterFst();
    }

    public Arc initArc(){

        return new Arc(Arc.State.NOT_FOUND, connfst.getFirstArc(new FST.Arc<>()));
    }

    public Long findInner(int seq1, int seq2) {

        try {
            return find(innerFst, seq1, seq2);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



//    public Long findOuter(int seq1, int seq2) {
//
//        try {
//            return find(outerFst, seq1, seq2);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    private Long find(FST<Long> fst, int seq1, int seq2) throws IOException {
        final FST.BytesReader fstReader = fst.getBytesReader();

        FST.Arc<Long> arc = new FST.Arc<>();

        arc = fst.getFirstArc(arc);
        Long output = fst.outputs.getNoOutput();

        Long outputs = null;

        //탐색 결과 없을때
        if (fst.findTargetArc(seq1, arc, arc, fstReader) != null) {
            output = fst.outputs.add(output, arc.output);

            if(fst.findTargetArc(seq2, arc, arc, fstReader) != null) {
                output = fst.outputs.add(output, arc.output);
            }else{
                return null;
            }

            if (arc.isFinal()) {

                //사전 매핑 정보 output
                outputs = fst.outputs.add(output, arc.nextFinalOutput);
            }
        }

        return outputs;
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
            if (connfst.findTargetArc(seq, follow, next, fstReader) != null) {
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


    public Long findConn(Term t1, Term t2) throws IOException {

        FST.Arc<Long> arc = new FST.Arc<>();

        arc = connfst.getFirstArc(arc);
        Long output = connfst.outputs.getNoOutput();

        Long outputs = null;

        for(Keyword keyword : t1.getKeywords()) {
            int seq = keyword.getSeq();

            //탐색 결과 없을때
            if (connfst.findTargetArc(seq, arc, arc, fstReader) == null) {
                return null;
            }

            output = connfst.outputs.add(output, arc.output);
        }

        int seq = t2.getFirst().getSeq();

        //탐색 결과 없을때
        if (connfst.findTargetArc(seq, arc, arc, fstReader) == null) {
            return null;
        }

        output = connfst.outputs.add(output, arc.output);

        if (arc.isFinal()) {

            //사전 매핑 정보 output
            outputs = connfst.outputs.add(output, arc.nextFinalOutput);
        }


        return outputs;
    }

}
