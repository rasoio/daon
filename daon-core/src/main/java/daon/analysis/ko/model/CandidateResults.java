package daon.analysis.ko.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 분석 결과 후보셋 한개
 */
public class CandidateResults implements Cloneable{

    private Logger logger = LoggerFactory.getLogger(CandidateResults.class);

    private long score;

    //매칭 된 표층형 어절
    private String surface;

    //연결 결과
    private List<Keyword> keywords = new ArrayList<>();

    //매칭 정보
    private List<ExplainInfo> explainInfos = new ArrayList<>();


    private boolean isExist;

    public void add(List<Keyword> keywords, ExplainInfo explainInfo){

        this.keywords.addAll(keywords);

        this.explainInfos.add(explainInfo);

        calculateScore();
    }

    public void calculateScore() {
        this.score = explainInfos.stream().mapToLong(ExplainInfo::getScore).sum();
    }

    public long getScore() {
        return score;
    }

    public List<ExplainInfo> getExplainInfos() {
        return explainInfos;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }

    public void setExplainInfos(List<ExplainInfo> explainInfos) {
        this.explainInfos = explainInfos;
    }

    @Override
    public String toString() {
        return "CandidateResults{ (" + hashCode() + ") " +
                "score=" + score +
                ", surface='" + surface + '\'' +
                ", keywords= \n" + StringUtils.join(keywords, System.lineSeparator()) +
                ", explainInfos= \n" + StringUtils.join(explainInfos, System.lineSeparator()) +
                ", isExist=" + isExist +
                '}';
    }

    @Override
    protected CandidateResults clone() {

        CandidateResults candidateResults = new CandidateResults();
        candidateResults.getKeywords().addAll(keywords);
        candidateResults.getExplainInfos().addAll(explainInfos);
        candidateResults.calculateScore();

        return candidateResults;
    }
}
