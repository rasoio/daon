package daon.analysis.ko.processor;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.*;
import daon.analysis.ko.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ConnectionProcessor {

    private Logger logger = LoggerFactory.getLogger(ConnectionProcessor.class);


    private ModelInfo modelInfo;

    public static ConnectionProcessor create(ModelInfo modelInfo) {

        return new ConnectionProcessor(modelInfo);
    }

    private ConnectionProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    /**
     * 최종 result 구성
     * @param prevResultInfo
     * @param resultInfo
     * @param nextResultInfo
     */
    public void process(ResultInfo prevResultInfo, ResultInfo resultInfo, ResultInfo nextResultInfo) {

        ConnectionFinder finder = new ConnectionFinder(modelInfo.getConnFst());

        int length = resultInfo.getLength();

        for(int offset = resultInfo.getOffset(); offset < length; offset++){

            CandidateTerms terms = resultInfo.getCandidateTerms(offset);

            if(terms == null) {
                continue;
            }

            TreeSet<CandidateSet> candidateSets = new TreeSet<>(scoreComparator);

            Term prev = getPrevTerm(offset, prevResultInfo, resultInfo);

            for (Term term : terms.getTerms()) {

                int nextIdx = offset + term.getLength();

                CandidateTerms nextTerms;

                boolean isNextEojeol = false;

                //마지막인 경우, 다음 어절의 0번째 offset에서 가져옴
                if(nextIdx == length && nextResultInfo != null){
                    nextTerms = findNextTerm(nextResultInfo, 0);
                    isNextEojeol = true;
                }else{
                    nextTerms = findNextTerm(resultInfo, nextIdx);
                }

                //nextTerms loop
                if(nextTerms == null ){
                    CandidateSet candidateSet = createCandidateSet(finder, prev, term, null, isNextEojeol);

                    candidateSets.add(candidateSet);
                }else{
                    for(Term next : nextTerms.getTerms()) {
                        CandidateSet candidateSet = createCandidateSet(finder, prev, term, next, isNextEojeol);

                        candidateSets.add(candidateSet);
                    }
                }

            }

            CandidateSet bestCandidateSet = candidateSets.first();

            //후보셋 정보 보기
            if(logger.isDebugEnabled()) {
                logger.debug("##############################");
                candidateSets.stream().limit(5).forEach(r -> logger.debug("result : {}", r));
            }

            addResultTerm(resultInfo, nextResultInfo, bestCandidateSet);

            //다음 offset 으로 이동
            offset = resultInfo.getOffset() - 1;

        }


    }

    private CandidateSet createCandidateSet(ConnectionFinder finder, Term prev, Term term, Term next, boolean isNextEojeol) {
        CandidateSet candidateSet = new CandidateSet(modelInfo, finder);

        candidateSet.setPrev(prev);
        candidateSet.setCur(term);
        candidateSet.setNext(next);

        candidateSet.setNextEojeol(isNextEojeol);

        candidateSet.calculateScore();
        return candidateSet;
    }

    private Term getPrevTerm(int offset, ResultInfo beforeResult, ResultInfo resultInfo) {
        Term prev = null;

        if(offset == 0){
            if(beforeResult != null) {
                prev = beforeResult.getLastTerm();
            }
        }else{
            prev = resultInfo.getLastTerm();
        }

        return prev;
    }


    private void addResultTerm(ResultInfo resultInfo, ResultInfo nextResult, CandidateSet bestCandidateSet) {
        //최종 결과 term 설정
        Term cur = bestCandidateSet.getCur();
        Term next = bestCandidateSet.getNext();
        Arc lastArc = bestCandidateSet.getLastArc();

        if(next == null){
            cur.setArc(lastArc);
        }else{
            next.setArc(lastArc);
        }

        resultInfo.addTerm(cur);

        if(bestCandidateSet.isNextEojeol()){

            nextResult.addTerm(next);
        }else{
            resultInfo.addTerm(next);

        }

    }


    private CandidateTerms findNextTerm(ResultInfo resultInfo, int offset){

        return resultInfo.getCandidateTerms(offset);
    }

    static final Comparator<CandidateSet> scoreComparator = (CandidateSet left, CandidateSet right) -> {
        return left.getScore() > right.getScore() ? -1 : 1;
    };


}
