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

            PrevInfo prevInfo = getPrevTerm(offset, prevResultInfo, resultInfo);

            for (Term term : terms.getTerms()) {

                int nextIdx = offset + term.getLength();

                NextInfo nextInfo = getNextTerms(nextIdx, length, resultInfo, nextResultInfo);

                addCandidateSet(finder, candidateSets, prevInfo, term, nextInfo);
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


    private void addCandidateSet(ConnectionFinder finder, Set<CandidateSet> candidateSets, PrevInfo prevInfo, Term term, NextInfo nextInfo){

        Term prev = prevInfo.prev;
        boolean isPrevEojeol = prevInfo.isPrevEojeol;

        CandidateTerms nextTerms = nextInfo.next;
        boolean isNextEojeol = nextInfo.isNextEojeol;

        //nextTerms loop
        if(nextTerms == null ){
            CandidateSet candidateSet = createCandidateSet(finder, prev, term, null, isPrevEojeol, isNextEojeol);

            candidateSets.add(candidateSet);
        }else{
            for(Term next : nextTerms.getTerms()) {
                CandidateSet candidateSet = createCandidateSet(finder, prev, term, next, isPrevEojeol, isNextEojeol);

                candidateSets.add(candidateSet);
            }
        }
    }

    private CandidateSet createCandidateSet(ConnectionFinder finder, Term prev, Term term, Term next, boolean isPrevEojeol, boolean isNextEojeol) {
        CandidateSet candidateSet = new CandidateSet(modelInfo, finder);

        candidateSet.setPrev(prev);
        candidateSet.setCur(term);
        candidateSet.setNext(next);

        candidateSet.setPrevEojeol(isPrevEojeol);
        candidateSet.setNextEojeol(isNextEojeol);

        candidateSet.calculateScore();
        return candidateSet;
    }

    private PrevInfo getPrevTerm(int offset, ResultInfo beforeResult, ResultInfo resultInfo) {

        PrevInfo info = new PrevInfo();

        if(offset == 0){
            if(beforeResult != null) {
                info.isPrevEojeol = true;
                info.prev = beforeResult.getLastTerm();
            }
        }else{
            info.prev = resultInfo.getLastTerm();
        }

        return info;
    }

    private NextInfo getNextTerms(int nextIdx, int length, ResultInfo resultInfo, ResultInfo nextResultInfo) {

        NextInfo info = new NextInfo();

        //마지막인 경우, 다음 어절의 0번째 offset에서 가져옴
        if(nextIdx == length && nextResultInfo != null){
            info.isNextEojeol = true;
            info.next = findNextTerm(nextResultInfo, 0);
        }else{
            info.next = findNextTerm(resultInfo, nextIdx);
        }

        return info;
    }


    private CandidateTerms findNextTerm(ResultInfo resultInfo, int offset){

        return resultInfo.getCandidateTerms(offset);
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

    static final Comparator<CandidateSet> scoreComparator = (CandidateSet left, CandidateSet right) -> {
        return left.getScore() > right.getScore() ? -1 : 1;
    };



    class PrevInfo {
        boolean isPrevEojeol;
        Term prev;
    }

    class NextInfo {
        boolean isNextEojeol;
        CandidateTerms next;
    }
}
