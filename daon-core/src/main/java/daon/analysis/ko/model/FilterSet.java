package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.processor.ConnectionFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 분석 결과 후보셋
 */
public class FilterSet {

    private Logger logger = LoggerFactory.getLogger(FilterSet.class);

    private float score;

    private int length;
    private int maxLength;

    private final static float DEFAULT_TAG_TRANS_SCORE = 0.00001f;
    private final static float DEFAULT_CONN_SCORE = 0.00001f;

    private final static float CONN_WEIGHT = 0.0001f;

    private ModelInfo modelInfo;
    private ConnectionFinder finder;

    private List<Term> terms = new ArrayList<>();

    private Term prev;

    private Term first;
    private Term last;

    public FilterSet(ModelInfo modelInfo, ConnectionFinder finder, int length) {
        this.modelInfo = modelInfo;
        this.finder = finder;
        this.maxLength = length;
    }


    public void add(Term term){

        Position position = Position.FIRST;

        if(term.getLength() == maxLength){
            position = FilterSet.Position.ALL;
        }

        float score = calculateScore(term, position);

        addTerm(term, score);

        first = term;
    }


    public void add(CandidateTerms candidateTerms){

        Term maxTerm = null;
        float maxScore = -1;

        for(Term term : candidateTerms.getTerms()) {

            Position position = Position.ING;

            //마지막 체크
            if(term.getLength() + term.getOffset() == maxLength){
                position = Position.LAST;
            }

            float score = calculateScore(term, position);

            if(maxScore < score){

                maxTerm = term;
                maxScore = score;

            }
        }

        addTerm(maxTerm, maxScore);
    }

    private void addTerm(Term term, float score){

        terms.add(term);
        prev = term;

        length += term.getLength();
        this.score += score;

        last = term;
    }


    private float calculateScore(Term cur, Position position) {

        long freq = calculateFreq(cur, position); // 사전 단어인 경우 freq 수치 조절 필요...
        float conn = calculateConn(cur, position);
        float tagTrans = calculateTagTrans(cur, position);

        return (freq * conn * tagTrans);
    }

    private float calculateConn(Term cur, Position position) {

        float score = DEFAULT_CONN_SCORE;

        if(prev != null){
            Long freq = getConnFreq(prev, cur);

            if(freq != null){
                long prevFreq = prev.getLast().getFreq();
                if(prevFreq == 0){
                    prevFreq = 1;
                }

                score = (float) freq / (float) prevFreq;
            }else {
//                score = CONN_WEIGHT * getTagTransScore(prev, cur);
                score = DEFAULT_CONN_SCORE;
            }
        }

        return score;

    }

    private float calculateTagTrans(Term cur, Position position) {

        float score = DEFAULT_TAG_TRANS_SCORE;

        if(position == Position.FIRST || position == Position.ALL){
            score = firstTagTransScore(cur.getFirst().getTag());
        }

        if(position == Position.LAST || position == Position.ALL){
            score = lastTagTransScore(cur.getLast().getTag());
        }

        if(position == Position.ING){
            if(prev != null){
                score = getTagTransScore(prev, cur);
            }
        }

        return score;
    }

    private long calculateFreq(Term cur, Position position) {
//        freq = getFreqScore(cur.getKeywords());
        return cur.getFreq();
    }

    private float getTagTransScore(Term t1, Term t2){

        float score = 0;

//        if(t1.isCompound() || t2.isCompound()){
//            score = connTagTransScore(t1.getLast().getTag(), t2.getFirst().getTag());
//        }else{
            score = middleTagTransScore(t1.getLast().getTag(), t2.getFirst().getTag());
//        }

        return score;
    }

    private Long getConnFreq(Term t1, Term t2){

        Long freq = null;

//        if(t1.isCompound() || t2.isCompound()){
//            freq = finder.findOuter(t1.getLast().getSeq(), t2.getFirst().getSeq());
//        }else{
            freq = finder.findInner(t1.getLast().getSeq(), t2.getFirst().getSeq());
//        }

        return freq;
    }



    public float getScore() {
        return score;
    }

    public int getLength() {
        return length;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public Term getFirst() {
        return first;
    }

    public Term getLast() {
        return last;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return "FilterSet (" + hashCode() + ") " + System.lineSeparator() +
                "score : " + score + System.lineSeparator() +
                "terms :  " + terms + System.lineSeparator() +
                "length :  " + length + System.lineSeparator()
                ;
    }


    private float firstTagTransScore(POSTag t){
        return modelInfo.getTagScore(POSTag.FIRST.name(), t.name());
    }

    private float lastTagTransScore(POSTag t){
        return modelInfo.getTagScore(t.name(), POSTag.LAST.name());
    }

    private float middleTagTransScore(POSTag t1, POSTag t2){
        return modelInfo.getTagScore(t1.name(), t2.name());
    }

    public enum Position {
        FIRST, ING, LAST, ALL
    }

}
