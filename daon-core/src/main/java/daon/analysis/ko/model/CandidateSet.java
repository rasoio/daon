package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.processor.ConnectionFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 분석 결과 후보셋
 */
public class CandidateSet {

    private Logger logger = LoggerFactory.getLogger(CandidateSet.class);

    private double score;

    private int arcCnt;

    private int length;

    private float freq;
    private float tagTrans;
    private int keywordCnt;

    private ModelInfo modelInfo;
    private ConnectionFinder finder;

    private Term prev;
    private Term cur;
    private Term next;

    private Arc prevArc;
    private Arc curArc;
    private Arc nextArc;
    private Arc lastArc;

    private boolean isNextEojeol;

    private double lengthScore;
    private double cntScore;
    private double freqScore;
    private double tagTransScore;
    private double connectionScore;

    public CandidateSet(ModelInfo modelInfo, ConnectionFinder finder) {
        this.modelInfo = modelInfo;
        this.finder = finder;
    }

    public void setNextEojeol(boolean nextEojeol) {
        isNextEojeol = nextEojeol;
    }

    public boolean isNextEojeol() {
        return isNextEojeol;
    }

    public void setPrev(Term prev) {
        this.prev = prev;
    }

    public void setCur(Term cur) {
        this.cur = cur;
        this.length = cur.getLength();
    }

    public void setNext(Term next) {
        if(next != null) {
            this.next = next;
            this.length += next.getLength();
        }
    }

    public Term getCur() {
        return cur;
    }

    public Term getNext() {
        return next;
    }

    public Arc getLastArc() {
        return lastArc;
    }

    public void calculateScore() {

//        if(logger.isDebugEnabled()) {
//            logger.debug("\n  prev term : {}, \n  cur term : {}, \n  next term : {}", prev, cur, next);
//        }

        calculateConnection();
//        calculateLength();
        calculateFreq();
        calculateTagTrans();
        calculateKeywordCnt();

        //스코어 속성
        // 1. 노출 빈도 (단어)

        // 2. 연결 수(유무) (connection arc)

        // 3. 태그 전이 확률 (tagTrans)

        // 4. 어절 전체 매칭, innerword 사전 가중치

        // 각 속성의 합산

        // 가중치 값 후보셋마다 다르게 구성 필요

        //term 길이를 중요하게 함.
        lengthScore = length * 0.001;
//        cntScore = -(keywordCnt * 0.000001);

        freqScore = freq * 0.0001;
        tagTransScore = tagTrans * 0.00000001;

        connectionScore = (arcCnt * 0.0001);

        score = lengthScore + cntScore + freqScore + tagTransScore + connectionScore;
    }

    private void calculateKeywordCnt() {
        keywordCnt = cur.getKeywords().length;

        if(next != null){
            keywordCnt += next.getKeywords().length;
        }
    }

    private void calculateTagTrans() {
        tagTrans = getTagTransScore(cur.getKeywords());

        if(next != null){
            tagTrans += getTagTransScore(next.getKeywords());
        }
    }

    private void calculateFreq() {
//        freq = getFreqScore(cur.getKeywords());
        freq = cur.getFreq();

        if(next != null){
//            freq += getFreqScore(next.getKeywords());
            freq += next.getFreq();
        }
    }

    private void calculateConnection() {

        prevArc = finder.initArc();

        if(prev != null) {
            prevArc = prev.getArc();

            //NOT_FOUND, FINAL 시 시작점으로 설정
            if(prevArc == null || prevArc.state != Arc.State.FOUND){
                prevArc = finder.initArc();
            }
        }

        curArc = find(cur, prevArc);

        lastArc = curArc;

        nextArc = finder.initArc();

        if(next != null){

            nextArc = find(next, curArc);

            lastArc = nextArc;
        }

        arcCnt = Math.min(curArc.cnt, 1) + Math.min(nextArc.cnt, 1);
    }

    private Arc find(Term term, Arc before){

        Arc after = null;

        Keyword[] keywords = term.getKeywords();
        int length = keywords.length;

        for (int i = 0; i < length; i++) {
            Keyword keyword = keywords[i];

            int seq = keyword.getSeq();

            if(i == 0) {
                after = finder.find(seq, before);
            }else{
                after = finder.find(seq, after);
            }

//            if(logger.isDebugEnabled()) {
//                logger.debug("find i : {}, before : {}, seq : {}, after : {}", i, before, modelInfo.getKeyword(seq), after);
//            }

            if(after.state == Arc.State.NOT_FOUND){
                break;
            }
        }

        return after;
    }

    public double getScore() {
        return score;
    }

    public int getLength() {
        return length;
    }


    public int getArcCnt() {
        return arcCnt;
    }


    @Override
    public String toString() {
        return "CandidateSet (" + hashCode() + ") " + System.lineSeparator() +
                "length   : " + lengthScore + System.lineSeparator() +
                "cnt      : " + cntScore + System.lineSeparator() +
                "freq     : " + freqScore + System.lineSeparator() +
                "tagTrans : " + tagTransScore + System.lineSeparator() +
                "arcCnt   : " + connectionScore + System.lineSeparator() +
                "score : " + String.format("%.10f", score) + System.lineSeparator() +
                "prev :  " + prev + " (arc : " + prevArc + ")" + System.lineSeparator() +
                "cur :  " + cur + " (arc : " + curArc + ")" + System.lineSeparator() +
                "next :  " + next + " (arc : " + nextArc + ")" + System.lineSeparator() +
                "lastArc :  " + lastArc + System.lineSeparator()
                ;
    }


    private float getFreqScore(Keyword... keywords){

        float score = 0f;
        for(Keyword keyword : keywords){
            score += ((float)keyword.getFreq() / modelInfo.getMaxFreq());
        }

        return score;
    }

    private float getTagTransScore(Keyword... keywords){

        float score = 0f;
        for(int i=0, len = keywords.length; i < len; i++){
            if(i == 0 && prev == null){
                score += modelInfo.getTagScore(POSTag.FIRST, keywords[i].getTag());
            } else if(i == (len -1) && next == null){
                score += modelInfo.getTagScore(keywords[i].getTag(), POSTag.LAST);
            } else if(i > 0 && len > 1){
                score += modelInfo.getTagScore(keywords[i-1], keywords[i]);
            } else{
//                score += modelInfo.getTagScore(keywords[i-1], keywords[i]);
            }
        }

        return score;
    }
}
