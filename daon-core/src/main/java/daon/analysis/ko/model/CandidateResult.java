package daon.analysis.ko.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 분석 결과 후보셋 한개
 */
public class CandidateResult implements Cloneable{

    private Logger logger = LoggerFactory.getLogger(CandidateResult.class);

    private long score;

    private int length;

    private List<CandidateTerm> terms = new ArrayList<>();

    //매칭 정보
    private List<ExplainInfo> explainInfos = new ArrayList<>();


    public void add(ExplainInfo explainInfo, CandidateTerm... terms){

        for (CandidateTerm term : terms) {
            this.terms.add(term);

            length += term.getLength();
        }

        this.explainInfos.add(explainInfo);

        calculateScore();
    }

    public void calculateScore() {
        this.score = explainInfos.stream().mapToLong(ExplainInfo::getScore).sum();
    }

    public long getScore() {
        return score;
    }

    public List<CandidateTerm> getTerms() {
        return terms;
    }

    public List<ExplainInfo> getExplainInfos() {
        return explainInfos;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "CandidateResult (" + hashCode() + ") " + System.lineSeparator() +
                "length : " + length + System.lineSeparator() +
                "score : " + score + System.lineSeparator() +
                "terms :  " + System.lineSeparator() + StringUtils.join(terms, System.lineSeparator()) + System.lineSeparator() +
                "explainInfos : " + System.lineSeparator() + StringUtils.join(explainInfos, System.lineSeparator()) + System.lineSeparator()
                ;
    }

    @Override
    protected CandidateResult clone() {

        CandidateResult candidateResult = new CandidateResult();
        candidateResult.getTerms().addAll(terms);
        candidateResult.getExplainInfos().addAll(explainInfos);

        candidateResult.setLength(length);
        candidateResult.calculateScore();

        return candidateResult;
    }
}
