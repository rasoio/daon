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
     * @param outerPrev
     * @param resultInfo
     */
    public void process(Keyword outerPrev, ResultInfo resultInfo) {

        final int length = resultInfo.getLength();

        //recurrent i => 인자
        for(int offset=0; offset< length; offset++) {

            ResultInfo.CandidateTerms curTerms = resultInfo.getCandidateTerms(offset);

            //분석 결과가 있는 경우
            if (curTerms != null) {

                //이전 term 찾기
                Term beforeTerm = resultInfo.getLastTerm();

                if(beforeTerm != null){

                    addNumberNounScore(curTerms, beforeTerm);

                    if(logger.isDebugEnabled()) {
                        logger.debug("before : {}", beforeTerm);
                    }
                }

                Keyword prev = null;

                if(offset == 0){
                    prev = outerPrev;
                }else{
                    //i > last result's last seq

                    if(beforeTerm != null) {
                        prev = beforeTerm.getLast();
                    }
                }

                //후보셋 스코어 정렬
                TreeSet<CandidateSet> candidateSets = new TreeSet<>(scoreComparator);

                find(prev, candidateSets, offset, curTerms, resultInfo);

                CandidateSet first = candidateSets.first();

                //후보셋 정보 보기
                if(logger.isDebugEnabled()) {
                    candidateSets.stream().limit(5).forEach(r -> logger.info("result : {}", r));
                }

                offset += (first.getLength() - 1);

                for(Term term : first.getTerms()) {
                    //최종 결과 term 설정
                    resultInfo.addTerm(term);
                }

            }

        }

    }

    private void addNumberNounScore(ResultInfo.CandidateTerms curTerms, Term beforeTerm) {

        //이전 term 이 숫자인지 체크, 숫자 다음 키워드가 의존명사, 수사인 경우 가중치 부여
        if(beforeTerm.getLast().getTag() == POSTag.SN){
            curTerms.getTerms().forEach(term ->{
               POSTag tag = term.getFirst().getTag();

               if(tag == POSTag.NNB || tag == POSTag.NR) {

                   float score = term.getExplainInfo().freqScore();
                   term.getExplainInfo().freqScore(score + 1f);
               }
            });
        }
    }

    private void find(Keyword prev, Set<CandidateSet> candidateSets, int offset, ResultInfo.CandidateTerms curTerms, ResultInfo resultInfo) {

        for (Term term : curTerms.getTerms()) {

            Keyword cur = term.getFirst();
            int length = term.getLength();

            CandidateSet candidateSet = findPrev(candidateSets, prev, cur, offset, term);

            final int nextOffset = offset + length;

            ResultInfo.CandidateTerms nextTerms = resultInfo.getCandidateTerms(nextOffset);

            //연결 확인이 가능할 경우
            if (nextTerms != null) {
                findNext(candidateSets, cur, nextTerms, candidateSet);
            }

        }

    }

    private CandidateSet findPrev(Set<CandidateSet> candidateSets, Keyword prev, Keyword cur, int offset, Term term) {
        CandidateSet candidateSet = new CandidateSet();

        if(prev != null && prev.getSeq() > 0){

            boolean isOuter = false;
            Float freqScore = null;
            int prevSeq = prev.getSeq();
            int seq = cur.getSeq();
            if(offset == 0){
                freqScore = findOuterSeq(prevSeq, seq);
                isOuter = true;
            }else{
                freqScore = findOuterSeq(prevSeq, seq);

                //음..
                if(freqScore == null){
                    freqScore = findInnerSeq(prevSeq, seq);
                }
            }

            if(freqScore != null) {

                float beforeFreqScore = term.getExplainInfo().freqScore();
                float beforeTagScore = term.getExplainInfo().tagScore();

                float freqWeight = 0.5f;

                //TODO tag score 도 합산할지 여부..
//                term.getExplainInfo().prevMatch(prevSeq, seq, isOuter).freqScore(freqScore + beforeFreqScore + freqWeight).tagScore(modelInfo.getTagScore(prev, cur) * 0.5f);
                term.getExplainInfo().prevMatch(prevSeq, seq, isOuter).freqScore(freqScore + beforeFreqScore + freqWeight).tagScore(modelInfo.getTagScore(prev, cur));
            }
        }

        candidateSet.add(term);

        candidateSets.add(candidateSet);

        return candidateSet;
    }


    private void findNext(Set<CandidateSet> queue, Keyword cur, ResultInfo.CandidateTerms nextTerms, CandidateSet candidateSet) {

        for (Term nextTerm : nextTerms.getTerms()) {

            Keyword next = nextTerm.getFirst();
            int nextLength = nextTerm.getLength();
            int seq = cur.getSeq();
            int nextSeq = next.getSeq();

            Float freqScore = findInnerSeq(seq, nextSeq);

            if(freqScore != null){

                CandidateSet nextCandidateSet = candidateSet.clone();

                float beforeFreqScore = nextTerm.getExplainInfo().freqScore();
                float beforeTagScore = nextTerm.getExplainInfo().tagScore();

                float freqWeight = 0.5f;

                nextTerm.getExplainInfo().nextMatch(seq, nextSeq).freqScore(freqScore).tagScore(modelInfo.getTagScore(cur, next));

                nextCandidateSet.add(nextTerm);

                queue.add(nextCandidateSet);

            }else{

                // 조합이 없는 경우 명사 + 명사 인 경우 조합할지 여부..  복합명사 조건...
                if(Utils.isTag(cur.getTag(), POSTag.N) && Utils.isTag(next.getTag(), POSTag.N)){
                    freqScore = 0.000000001f; // 조합 이 없는 케이스인데 임의로 지정함 가장 낮은 점수 부여

                    CandidateSet nextCandidateSet = candidateSet.clone();

                    float beforeFreqScore = nextTerm.getExplainInfo().freqScore();
                    float beforeTagScore = nextTerm.getExplainInfo().tagScore();

                    float freqWeight = 0.5f;

                    nextTerm.getExplainInfo().compoundMatch(seq, nextSeq).freqScore(freqScore).tagScore(modelInfo.getTagScore(cur, next));

                    nextCandidateSet.add(nextTerm);

                    queue.add(nextCandidateSet);
                }
            }

        }

    }

    /**
     * 최대 스코어 사용
     * 우선순위 (최장일치)
     * 1. 표층형이 가장 길게 연결된 결과
     * 2. 표층형이 가장 길게 매칭된 사전 결과

     * 같은 길이 일때
     * 최대 스코어 값 사용
     * 연결 매칭 점수 - 연결 빈도, 태그 결합 빈도 (누적 점수)
     * 단일 사전 점수 - 노출 빈도, 단일 태그 노출 빈도
     */
    static final Comparator<CandidateSet> scoreComparator = (left, right) -> {

        //최장일치 우선
        if(left.getLength() > right.getLength()){
            return -1;
        }else{

            //같은 길이인 경우 높은 스코어 우선
            if(left.getLength() == right.getLength()){
                return left.getScore() > right.getScore() ? -1 : 1;
            }else{
                return 1;
            }
        }
    };

    private Float findOuterSeq(int outerPrevSeq, int seq){

        String key = outerPrevSeq + "|" + seq;

        Float cnt = modelInfo.getOuter().get(key.hashCode());

        return cnt;
    }

    private Float findInnerSeq(int seq, int nSeq){

        String key = seq + "|" + nSeq;

        Float cnt = modelInfo.getInner().get(key.hashCode());

        return cnt;
    }
}
