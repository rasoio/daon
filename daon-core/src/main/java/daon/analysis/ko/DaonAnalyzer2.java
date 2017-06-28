package daon.analysis.ko;

import daon.analysis.ko.model.*;
import daon.analysis.ko.processor.ConnectionFinder;
import daon.analysis.ko.processor.DictionaryProcessor;
import daon.analysis.ko.processor.UnknownProcessor;
import org.apache.lucene.util.fst.FST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ResultInfo nextResultInfo = null;

        //tokenizer results
        for(int i=0,len = eojeols.length; i<len; i++){

            String eojeol = eojeols[i];

            EojeolInfo info = new EojeolInfo();

            ResultInfo curResultInfo = createResultInfo(eojeol);

            if(nextResultInfo == null) {
                fillResultInfo(curResultInfo);
            }else{
                curResultInfo = nextResultInfo;
            }

            String nextEojeol = "";
            if(i < (len -1)){
                nextEojeol = eojeols[i+1];
                nextResultInfo = createResultInfo(nextEojeol);
                fillResultInfo(nextResultInfo);
            }else{
                nextResultInfo = null;
            }

            connectionProcess(beforeResult, curResultInfo, nextResultInfo);

            List<Term> terms = curResultInfo.getTerms();

            beforeResult = curResultInfo;

            info.setEojeol(eojeol);
            info.setTerms(terms);

            eojeolInfos.add(info);


        }

//        for(String eojeol : eojeols) {
//        }

        return eojeolInfos;
    }

    private ResultInfo createResultInfo(String eojeol){
        char[] chars = eojeol.toCharArray();
        int length = chars.length;

        return ResultInfo.create(chars, length);
    }

    private void fillResultInfo(ResultInfo resultInfo) throws IOException {
        //사전 탐색 결과
        DictionaryProcessor.create(modelInfo).process(resultInfo);

        //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
        UnknownProcessor.create().process(resultInfo);
    }

    private void connectionProcess(ResultInfo beforeResult, ResultInfo resultInfo, ResultInfo nextResult) throws IOException {

        //어절 두개를 가지고 체크 필요 =>
        ConnectionFinder finder = new ConnectionFinder(fst);

        int length = resultInfo.getLength();
        for(int offset = resultInfo.getOffset(); offset < length; offset++){

            CandidateTerms terms = resultInfo.getCandidateTerms(offset);

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

                    CandidateTerms nextTerms;

                    boolean isNextEojeol = false;

                    //마지막인 경우
                    if(nextIdx == length && nextResult != null){
                        nextTerms = findNextTerm(nextResult, 0);
                        isNextEojeol = true;
                    }else{
                        nextTerms = findNextTerm(resultInfo, nextIdx);
                    }


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

                            candidateSet.setNextEojeol(isNextEojeol);

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
                    candidateSets.stream().limit(5).forEach(r -> logger.debug("result : {}", r));
                }

                addResultTerm(resultInfo, nextResult, bestCandidateSet);

                offset = resultInfo.getOffset() - 1;

            }

        }

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



}
