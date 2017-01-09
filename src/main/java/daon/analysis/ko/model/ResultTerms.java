package daon.analysis.ko.model;

import daon.analysis.ko.dict.BaseDictionary;
import daon.analysis.ko.score.Scorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 분석 결과
 */
public class ResultTerms {


    private Logger logger = LoggerFactory.getLogger(ResultTerms.class);

    private Map<Integer, List<Term>> resultsMap = new HashMap<Integer, List<Term>>();

    private List<Term> results = new ArrayList<Term>();

    private Scorer scorer;

    private int lastOffset = -1;

    public ResultTerms(Scorer scorer) {
        this.scorer = scorer;
    }

    public void add(int offset, Term term) {
        int termLength = term.getLength();

        int endOffset = offset + termLength;

        List<Term> prevTerms = resultsMap.get(offset);

        float prevMinScore = Float.MAX_VALUE;
        Term prevMinTerm = null;

        if(prevTerms != null) {
            for (Term prevTerm : prevTerms) {

                float score = scorer.score(prevTerm, term);

                if(score < prevMinScore){
                    prevMinScore = score;
                    prevMinTerm = prevTerm;
                }

            }
        }


        if(prevMinTerm != null){
            term.setScore(prevMinScore);
            term.setPrevTerm(prevMinTerm);
        }


        List<Term> list = resultsMap.get(endOffset);

        if(list == null){
            list = new ArrayList<>();
        }

        list.add(term);

        resultsMap.put(endOffset, list);

        lastOffset = endOffset;
    }


    public void findBestPath(){


        List<Term> lastList = resultsMap.get(lastOffset);

        for(Term term : lastList){

            List<Term> list = new ArrayList<>();
            list.add(term);
            Term prevTerm;
            while((prevTerm = term.getPrevTerm()) != null) {
                list.add(prevTerm);
                term = prevTerm;
            }

            Collections.sort(list, (o1, o2) -> o1.getOffset() - o2.getOffset());

            logger.info("last term list : {}", list);

        }


    }


    public List<Term> getResults() {
        return results;
    }

    public void setResults(List<Term> results) {
        this.results = results;
    }

}
