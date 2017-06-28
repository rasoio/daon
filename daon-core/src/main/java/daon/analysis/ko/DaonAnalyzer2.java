package daon.analysis.ko;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.*;
import daon.analysis.ko.processor.ConnectionFinder;
import daon.analysis.ko.processor.ConnectionProcessor;
import daon.analysis.ko.processor.DictionaryProcessor;
import daon.analysis.ko.processor.UnknownProcessor;
import daon.analysis.ko.util.Utils;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class DaonAnalyzer2 implements Serializable{

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer2.class);

    private ModelInfo modelInfo;
    private FST<Long> fst;


    public DaonAnalyzer2(ModelInfo modelInfo) throws IOException {

        this.modelInfo = modelInfo;

        this.fst = modelInfo.getConnFst();
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }



    public List<EojeolInfo> analyzeText(String text) throws IOException {

        List<EojeolInfo> eojeolInfos = new ArrayList<>();

        String[] eojeols = text.split("\\s");

        ResultInfo beforeResult = null;


        //tokenizer results
        for(String eojeol : eojeols) {

            EojeolInfo info = new EojeolInfo();

            char[] chars = eojeol.toCharArray();
            int length = chars.length;

            ResultInfo resultInfo = ResultInfo.create(chars, length);

            process(beforeResult, resultInfo);

            List<Term> terms = resultInfo.getTerms();

            beforeResult = resultInfo;

            info.setEojeol(eojeol);
            info.setTerms(terms);

            eojeolInfos.add(info);

        }

        return eojeolInfos;
    }

    private void process(ResultInfo beforeResult, ResultInfo resultInfo) throws IOException {


        //사전 탐색 결과
        DictionaryProcessor.create(modelInfo).process(resultInfo);

        //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
        UnknownProcessor.create().process(resultInfo);

        //어절 두개를 가지고 체크 필요 =>
        ConnectionFinder finder = new ConnectionFinder(fst);

        for(int offset = 0; offset < resultInfo.getLength(); offset++){

            CandidateTerms terms = resultInfo.getCandidateTerms(offset);

            //같은 offset 인 경우 같은 prevTerms 임. 매번 find 하지 않도록 변경
//            CandidateTerms prevTerms = findPrevTerm(resultInfo, offset);


            if(terms != null) {

                TreeSet<CandidateSet> candidateSets = new TreeSet<>(scoreComparator);

                Term prev = resultInfo.getLastTerm();

                for (Term term : terms.getTerms()) {

                    if(offset == 0){
                        if(beforeResult != null) {
                            Term lastTerm = beforeResult.getLastTerm();
                            prev = lastTerm;
                        }
                    }

                    int nextIdx = offset + term.getLength();

                    CandidateTerms nextTerms = findNextTerm(resultInfo, nextIdx);

                    //후보셋 스코어 정렬

                    //nextTerms loop
                    if(nextTerms == null ){
                        //할당
                        CandidateSet candidateSet = new CandidateSet(modelInfo, finder);

                        candidateSet.setEojeolLength(resultInfo.getLength());

                        candidateSet.setPrev(prev);
                        candidateSet.setCur(term);

                        candidateSet.calculateScore();

                        candidateSets.add(candidateSet);
                    }else{

                        for(Term next : nextTerms.getTerms()) {
                            //할당
                            CandidateSet candidateSet = new CandidateSet(modelInfo, finder);

                            candidateSet.setEojeolLength(resultInfo.getLength());

                            candidateSet.setPrev(prev);
                            candidateSet.setCur(term);
                            candidateSet.setNext(next);

                            candidateSet.calculateScore();
                            //term 과 next 사이에 갭이 없으면 next 도 할당할까?

                            candidateSets.add(candidateSet);
                        }
                    }

                }

                CandidateSet bestCandidateSet = candidateSets.first();

                //후보셋 정보 보기
                if(logger.isDebugEnabled()) {
                    logger.debug("##############################");
                    candidateSets.stream().limit(10).forEach(r -> logger.debug("result : {}", r));
                }

                offset += (bestCandidateSet.getLength() -1);

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
                resultInfo.addTerm(next);


//                for (Term r : first.getTerms()) {
//                    resultInfo.addTerm(r);
//                }
            }

        }


        /*
        //연결 추출
        for(int offset = 0; offset < resultInfo.getLength(); offset++) {

            CandidateTerms terms = resultInfo.getCandidateTerms(offset);

            if(terms != null) {

                //후보셋 스코어 정렬
                TreeSet<CandidateSet> candidateSets = new TreeSet<>(scoreComparator);

                //할당
                for(Term term : terms.getTerms()) {

                    CandidateSet candidateSet = new CandidateSet();

                    candidateSet.setEojeolLength(resultInfo.getLength());


//                    Term prev = term.getPrevTerm();
                    Term next = term.getNextTerm();

                    int nextOffset = -1;
                    if(next != null){
                        nextOffset = next.getOffset();
                    }
                    int termLast = term.getOffset() + term.getLength();

                    if(termLast == nextOffset){
                        candidateSet.add(term, next);
                    }else{
                        candidateSet.add(term);
                    }
                    //term 과 next 사이에 갭이 없으면 next 도 할당할까?

                    candidateSets.add(candidateSet);
                }

                CandidateSet first = candidateSets.first();

                //후보셋 정보 보기
                logger.debug("##############################");
                candidateSets.stream().limit(5).forEach(r -> logger.debug("result : {}", r));

                offset = (first.getLength() -1);

                for (Term term : first.getTerms()) {
                    //최종 결과 term 설정
                    resultInfo.addTerm(term);
                }
            }
        }
        */


//        outerPrev = findPrevTerm(resultInfo, length);

//        resultTerms.addAll(resultInfo.getTerms());

//        return outerPrev;


    }


    private CandidateTerms findPrevTerm(ResultInfo resultInfo, int offset){

        CandidateTerms terms = resultInfo.getPrevCandidateTerms(offset);

        return terms;
    }

    private CandidateTerms findNextTerm(ResultInfo resultInfo, int offset){

        CandidateTerms terms = resultInfo.getCandidateTerms(offset);

        return terms;
    }


    private int getPrevOffset(int offset, CandidateTerms[] prevCandidateTerms){
//        CandidateTerms terms = prevCandidateTerms[offset];
//
//        if(terms != null){
//            Term t = terms.getTerms().get(0);
//
//            int seq = t.getFirst().getSeq();
//            POSTag tag = t.getFirst().getTag();
//
//            if(seq == 0 || Utils.isTag(tag, POSTag.S)){
//                offset = t.getOffset();
//
//                return getPrevOffset(offset, prevCandidateTerms);
//            }
//        }

        return offset;
    }



    static final Comparator<CandidateSet> scoreComparator = (left, right) -> {


        return left.getScore() > right.getScore() ? -1 : 1;

        /*
        //연결 arc 수 우선
        //최장일치 우선
//        if(left.getLength() > right.getLength()){
        if(left.getArcCnt() > right.getArcCnt()){
            return -1;
        }else{

            //같은 길이인 경우 높은 스코어 우선
//            if(left.getLength() == right.getLength()){
            if(left.getArcCnt() == right.getArcCnt()){
                return left.getScore() > right.getScore() ? -1 : 1;
            }else{
                return 1;
            }
        }
        */
    };

    private void findArc(ConnectionFinder finder, Term term, Arc followArc, Term prevTerm) {
        Arc findArc = null;


        //연결 term 이 없는 경우 cnt = 1 로 지정
        for (int i = 0; i < term.getKeywords().length; i++) {
            Keyword keyword = term.getKeywords()[i];

            int seq = keyword.getSeq();

            if(i == 0) {
                findArc = finder.find(seq, followArc);
            }else{
                findArc = finder.find(seq, findArc);
            }

            if(findArc.state == Arc.State.NOT_FOUND){
                break;
            }

//            logger.info("findArc check i : {}, prev_seq : {}, seq : {}, find : {}, cnt : {}", i, (i==0) ? followArc : findArc, modelInfo.getKeyword(seq), findArc.state, findArc.cnt);
        }


        if(prevTerm == null){
//            if(findArc.cnt <= 2) {
//                findArc.cnt = 1;
//            }

        }



        //연결 존재하는 경우 마지막 최종 만 남음...
        if (findArc != null && findArc.state != Arc.State.NOT_FOUND) {

            boolean isUpdate = true;
            Arc beforeArc = term.getArc();

            if(beforeArc != null){
                if(beforeArc.cnt > findArc.cnt){
                    isUpdate = false;
                }

                //진행중 상태 우선순위 높임
//                if(beforeArc.state == State.FOUND){
//                    isUpdate = false;
//                }
            }

            //연결
            if(isUpdate) {
                term.setArc(findArc);

                if (prevTerm != null) {

                    term.setPrevTerm(prevTerm);
                    prevTerm.setNextTerm(term);
                }
            }

//            logger.info("findArc update !! \n prev : {}, \n term : {}, \n find : {}, cnt : {}, isUpdate : {}", prevTerm, term, findArc.state, findArc.cnt, isUpdate);
        }

    }


    /**
     *
     * @param finder
     * @param term 현재 term
     * @param followArc 이전 term의 arc
     */
    private void findArc(ConnectionFinder finder, Term term, Arc followArc) {

        findArc(finder, term, followArc, null);
    }





}
