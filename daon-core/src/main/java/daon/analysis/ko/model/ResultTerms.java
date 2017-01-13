package daon.analysis.ko.model;

import daon.analysis.ko.score.Scorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 분석 결과
 * TODO resultsMap => 다른 배열 객체로 변경.
 */
public class ResultTerms {


    private Logger logger = LoggerFactory.getLogger(ResultTerms.class);

//    private Map<Integer, List<Term>> resultsMap = new HashMap<Integer, List<Term>>();
    private Result[] resultsMap = new Result[8];

    private List<Term> results = new ArrayList<Term>();

    private Scorer scorer;

    private int lastOffset = -1;

    public ResultTerms(Scorer scorer) {
        this.scorer = scorer;
    }

    public void add(int offset, Term term) {
        int termLength = term.getLength();

        int endOffset = offset + termLength;

        resize(endOffset);

        Result prevResult = resultsMap[offset];

        //불필요 처리 terms
        if(offset > 0 && prevResult == null){
            return;
        }

        float prevMinScore = Float.MAX_VALUE;

        if(prevResult != null) {
            Term prevMinTerm = null;
            for (Term prevTerm : prevResult.getTerms()) {

                //누적 스코어 계산
                float score = scorer.score(prevTerm, term);

//                logger.info("score : {}, prevTerm : {}, term : {}", score, prevTerm, term);

                // TODO tie-breaker
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


        Result result = resultsMap[endOffset];
//        List<Term> list = resultsMap.get(endOffset);

        if(result == null){
            result = new Result();
        }

        result.add(term);
//        list.add(term);

        resultsMap[endOffset] = result;
//        resultsMap.put(endOffset, list);

        lastOffset = endOffset;
    }

    private void resize(int endOffset) {
        if(endOffset >= resultsMap.length){
            Result[] newArray = new Result[endOffset + 10];
            System.arraycopy(resultsMap, 0, newArray, 0, resultsMap.length);

            resultsMap = newArray;
        }
    }


    public void findBestPath(){

        //추가된게 없는 경우..
        if(lastOffset == -1){
            return;
        }

        Result lastList = resultsMap[lastOffset];

        if(lastList == null){
            return;
        }

        Term bestTerm = null;

        float lowerScore = Float.MAX_VALUE;

        for(Term term : lastList.getTerms()){

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

            Collections.reverse(list);
//            Collections.sort(list, (o1, o2) -> o1.getOffset() - o2.getOffset());

            results = list;
        }


//        logger.info("best list : {}", results);
    }


    public List<Term> getResults() {
        return results;
    }


    public class Result {
        List<Term> terms = new ArrayList<Term>();


        public List<Term> getTerms() {
            return terms;
        }

        public void setTerms(List<Term> terms) {
            this.terms = terms;
        }

        public void add(Term term){
            this.terms.add(term);
        }
    }

}
