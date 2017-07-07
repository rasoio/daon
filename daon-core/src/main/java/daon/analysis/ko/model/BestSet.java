package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.processor.ConnectionFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 분석 결과 후보셋
 */
public class BestSet {

    private Logger logger = LoggerFactory.getLogger(BestSet.class);

    private double score;


    private final static float CONN_WEIGHT = 0.0001f;

    private ModelInfo modelInfo;
    private ConnectionFinder finder;

//    private FilterSet prev;
    private FilterSet cur;
    private FilterSet next;

    private float conn;
    private float tagTrans;
    private float base;

    public BestSet(ModelInfo modelInfo, ConnectionFinder finder) {
        this.modelInfo = modelInfo;
        this.finder = finder;
    }

    public FilterSet getCur() {
        return cur;
    }

    public void setCur(FilterSet cur) {
        this.cur = cur;
    }

    public FilterSet getNext() {
        return next;
    }

    public void setNext(FilterSet next) {
        this.next = next;
    }

    public void calculateScore() {

        conn = calculateConn();
//        float tagTrans = calculateTagTrans();

        base = calculateBase();

        score = base * conn;
    }


    private float calculateBase() {
        float score = cur.getScore();

        if(next != null){
            score += next.getScore();
        }

        return score;
    }

    private float calculateConn() {

        float score = 1;

        if(next != null){

            //이전 어절 참조 여부 : true, connTag
            Float connScore = getConnFreq(cur.getLast(), next.getFirst());

            if(connScore != null){
                score = connScore;
            }else{
                score = CONN_WEIGHT * connTagTransScore(cur.getLast().getLast().getTag(), next.getFirst().getFirst().getTag());
            }

        }

        return score;
    }

    private Float getConnFreq(Term t1, Term t2){

        Float score = null;
        try {
            Long freq = finder.findConn(cur.getLast(), next.getFirst());

            if(freq != null) {
                long curFreq = cur.getLast().getFreq();
                if (curFreq == 0) {
                    curFreq = 1;
                }
                score = 1.2f * (float) freq / (float) curFreq; // 일치 가중치 부여?
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(score == null){

            Long freq = finder.findOuter(cur.getLast().getLast().getSeq(), next.getFirst().getFirst().getSeq());
            if(freq != null){
                long curFreq = cur.getLast().getLast().getFreq();
                if(curFreq == 0){
                    curFreq = 1;
                }
                score = (float) freq / (float) curFreq;
            }
        }

        return score;
    }


    public double getScore() {
        return score;
    }



    @Override
    public String toString() {
        return "BestSet (" + hashCode() + ") " + System.lineSeparator() +
                "conn : " + conn + System.lineSeparator() +
//                "tagTrans : " + tagTrans + System.lineSeparator() +
                "base : " + base + System.lineSeparator() +
                "score : " + score + System.lineSeparator() +
                "cur :  " + cur + System.lineSeparator() +
                "next :  " + next + System.lineSeparator()
                ;
    }


    private float connTagTransScore(POSTag t1, POSTag t2){
        String t = t1.name() + "|END";

        return modelInfo.getTagScore(t, t2.name());
    }

}
