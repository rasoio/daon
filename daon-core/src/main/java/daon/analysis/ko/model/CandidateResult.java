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

    private double score;

    private int length;

    private List<CandidateTerm> terms = new ArrayList<>();

    public void add(CandidateTerm... terms){

        for (CandidateTerm term : terms) {
            this.terms.add(term);

            length += term.getLength();
        }

        calculateScore();
    }

    public void calculateScore() {

        // sum explain score
        this.score = terms.stream().mapToDouble(t -> {
            return t.getExplainInfo().getScore();
        }).sum();
    }

    public double getScore() {
        return score;
    }

    public List<CandidateTerm> getTerms() {
        return terms;
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
                "score : " + String.format("%.5f", score) + System.lineSeparator() +
                "terms :  " + System.lineSeparator() + StringUtils.join(terms, System.lineSeparator()) + System.lineSeparator()
                ;
    }

    @Override
    protected CandidateResult clone() {

        CandidateResult candidateResult = new CandidateResult();
        candidateResult.getTerms().addAll(terms);

        candidateResult.setLength(length);
        candidateResult.calculateScore();

        return candidateResult;
    }
}
