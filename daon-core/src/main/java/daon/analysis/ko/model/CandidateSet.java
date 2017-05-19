package daon.analysis.ko.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 분석 결과 후보셋 한개
 */
public class CandidateSet implements Cloneable{

    private Logger logger = LoggerFactory.getLogger(CandidateSet.class);

    private double score;

    private int length;

    private List<Term> terms = new ArrayList<>();

    public void add(Term... terms){

        for (Term term : terms) {
            this.terms.add(term);

            length += term.getLength();
        }

        calculateScore();
    }

    public void calculateScore() {

        // sum explain score
        this.score = terms.stream().mapToDouble(t -> {
            return t.getExplainInfo().score();
        }).sum();
    }

    public double getScore() {
        return score;
    }

    public List<Term> getTerms() {
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
        return "CandidateSet (" + hashCode() + ") " + System.lineSeparator() +
                "length : " + length + System.lineSeparator() +
                "score : " + String.format("%.5f", score) + System.lineSeparator() +
                "terms :  " + System.lineSeparator() + StringUtils.join(terms, System.lineSeparator()) + System.lineSeparator()
                ;
    }

    @Override
    public CandidateSet clone() {

        CandidateSet candidateSet = new CandidateSet();
        candidateSet.getTerms().addAll(terms);

        candidateSet.setLength(length);
        candidateSet.calculateScore();

        return candidateSet;
    }
}
