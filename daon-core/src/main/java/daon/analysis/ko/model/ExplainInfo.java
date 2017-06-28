package daon.analysis.ko.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * term 분석 정보
 */
public class ExplainInfo {

    private Logger logger = LoggerFactory.getLogger(ExplainInfo.class);

    //매칭 된 seq 정보
    private MatchInfo matchInfo;

    public static ExplainInfo create() {

        return new ExplainInfo();
    }

    private ExplainInfo() {}

    public ExplainInfo dictionaryMatch(int... seq){
        matchInfo = MatchInfo.getInstance(MatchInfo.MatchType.DICTIONARY).setSeqs(seq);
        return this;
    }

    public ExplainInfo wordsMatch(int... seq){
        matchInfo = MatchInfo.getInstance(MatchInfo.MatchType.WORDS).setSeqs(seq);
        return this;
    }

    public ExplainInfo unknownMatch(){
        matchInfo = MatchInfo.getInstance(MatchInfo.MatchType.UNKNOWN);
        return this;
    }

    public MatchInfo getMatchInfo() {
        return matchInfo;
    }


    @Override
    public String toString() {
        return "{" +
                "matchInfo=" + matchInfo +
                '}';
    }
}
