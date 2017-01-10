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

    public List<Term> get(int offset){
        return resultsMap.get(offset);
    }

    public void add(int offset, Term term) {
        int termLength = term.getLength();

        int endOffset = offset + termLength;

        List<Term> prevTerms = resultsMap.get(offset);

        //불필요 처리 terms
        if(offset > 0 && prevTerms == null){
            return;
        }

        float prevMinScore = Float.MAX_VALUE;

        if(prevTerms != null) {
            Term prevMinTerm = null;
            for (Term prevTerm : prevTerms) {

                float score = scorer.score(prevTerm, term);

//                logger.info("score : {}, prevTerm : {}, term : {}", score, prevTerm, term);

                if(score < prevMinScore){
                    prevMinScore = score;
                    prevMinTerm = prevTerm;
                }

            }

            if(prevMinTerm != null) {
                term.setScore(prevMinScore);
                term.setPrevTerm(prevMinTerm);
            }
        }else{
            float score = scorer.score(null, term);
            term.setScore(score);
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

        if(lastList == null){
            return;
        }

        Term bestTerm = null;

        float lowerScore = Float.MAX_VALUE;

        for(Term term : lastList){

            float score = term.getScore();
//            logger.info("last term list : {}", list);

            if(score < lowerScore){
                lowerScore = score;
                bestTerm = term;
            }

        }


        if(bestTerm != null){

            List<Term> list = new ArrayList<>();
            list.add(bestTerm);
            Term prevTerm;
            while((prevTerm = bestTerm.getPrevTerm()) != null) {
                list.add(prevTerm);
                bestTerm = prevTerm;
            }

            Collections.sort(list, (o1, o2) -> o1.getOffset() - o2.getOffset());

            results = list;
        }


//        logger.info("best list : {}", results);
    }


    public List<Term> getResults() {
        return results;
    }

}
