package daon.analysis.ko.model;

import daon.analysis.ko.reader.ModelReader;
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


    public static ExplainInfo create() {

        return new ExplainInfo();
    }

    private ExplainInfo() {}

    public ExplainInfo dictionaryMatch(int[] seq){
        matchInfo = MatchInfo.getInstance(MatchInfo.MatchType.DICTIONARY).setSeqs(seq);
        return this;
    }

    public ExplainInfo prevMatch(int prevSeq, int seq, boolean isOuter){
        matchInfo = MatchInfo.getInstance(MatchInfo.MatchType.PREV_CONNECTION).setPrevSeq(prevSeq).setSeq(seq).setOuter(isOuter);
        return this;
    }

    public ExplainInfo nextMatch(int seq, int nextSeq){
        matchInfo = MatchInfo.getInstance(MatchInfo.MatchType.NEXT_CONNECTION).setSeq(seq).setNextSeq(nextSeq);
        return this;
    }

    public ExplainInfo unknownMatch(){
        matchInfo = MatchInfo.getInstance(MatchInfo.MatchType.UNKNOWN);
        return this;
    }

    public MatchInfo getMatchInfo() {
        return matchInfo;
    }

    public float freqScore() {
        return freqScore;
    }

    public ExplainInfo freqScore(float freqScore) {
        this.freqScore = freqScore;
        return this;
    }

    public float tagScore() {
        return tagScore;
    }

    public ExplainInfo tagScore(float tagScore) {
        this.tagScore = tagScore;
        return this;
    }

    public double score(){
        return freqScore * tagScore;
//        return freqScore + tagScore;
    }

    @Override
    public String toString() {
        return "{" +
                "matchInfo=" + matchInfo +
                ", freqScore=" + String.format("%.5f", freqScore) +
                ", tagScore=" + String.format("%.5f", tagScore) +
                ", score=" + String.format("%.5f", score()) +
                '}';
    }
}
