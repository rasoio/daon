package daon.analysis.ko.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 분석 결과 후보셋
 */
public class ExplainInfo {

    private Logger logger = LoggerFactory.getLogger(ExplainInfo.class);

    //매칭 된 seq 정보
    private MatchInfo matchInfo;

    //노출 점수 : 빈도 ( 연결 노출, 사전 노출 )
    private float freqScore;

    //태그 점수 : 빈도 ( 태그 연결 빈도, 도립 태그 노출 빈도 )
    private float tagScore;

    public MatchInfo createDictionaryMatchInfo(int[] seq){
        return MatchInfo.getInstance(MatchInfo.MatchType.DICTIONARY).setSeqs(seq);
    }

    public MatchInfo createPrevMatchInfo(int prevSeq, int seq, boolean isOuter){
        return MatchInfo.getInstance(MatchInfo.MatchType.PREV_CONNECTION).setPrevSeq(prevSeq).setSeq(seq).setOuter(isOuter);
    }

    public MatchInfo createNextMatchInfo(int seq, int nextSeq){
        return MatchInfo.getInstance(MatchInfo.MatchType.NEXT_CONNECTION).setSeq(seq).setNextSeq(nextSeq);
    }

    public MatchInfo createUnknownMatchInfo(){
        return MatchInfo.getInstance(MatchInfo.MatchType.UNKNOWN);
    }

    public MatchInfo getMatchInfo() {
        return matchInfo;
    }

    public void setMatchInfo(MatchInfo matchInfo) {
        this.matchInfo = matchInfo;
    }

    public float getFreqScore() {
        return freqScore;
    }

    public void setFreqScore(float freqScore) {
        this.freqScore = freqScore;
    }

    public float getTagScore() {
        return tagScore;
    }

    public void setTagScore(float tagScore) {
        this.tagScore = tagScore;
    }

    public double getScore(){
        return freqScore * tagScore;
//        return freqScore + tagScore;
    }

    @Override
    public String toString() {
        return "{" +
                "matchInfo=" + matchInfo +
                ", freqScore=" + String.format("%.5f", freqScore) +
                ", tagScore=" + String.format("%.5f", tagScore) +
                ", score=" + String.format("%.5f", getScore()) +
                '}';
    }
}
