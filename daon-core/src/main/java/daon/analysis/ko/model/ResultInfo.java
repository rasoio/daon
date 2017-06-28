package daon.analysis.ko.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

/**
 * 사전 분석 결과 정보
 *
 */
public class ResultInfo {
    private Logger logger = LoggerFactory.getLogger(ResultInfo.class);

    private final char[] chars;
    private final int length;
    private int offset = 0;

    private final boolean[] checkBits;

    //후보셋
    private final CandidateTerms[] candidateTerms;

    //최종 결과셋
    private List<Term> terms = new LinkedList<>();

    private ResultInfo(char[] chars, int length) {
        this.chars = chars;
        this.length = length;
        checkBits = new boolean[length];
        candidateTerms = new CandidateTerms[length + 1];
    }

    public static ResultInfo create(char[] chars, int length){
        return new ResultInfo(chars, length);
    }

    public char[] getChars() {
        return chars;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    public void addCandidateTerm(Term term){

        //start
        int offset = term.getOffset();

        int length = term.getLength();

        //end
        int end = offset + length;

        int idx = offset;

        //중복 term 방지 처리

//        if(checkBits[offset] && checkBits[end - 1]) {
//            return;
//        }


        if(!checkBits[offset] || !checkBits[end - 1]) {
            IntStream.range(offset, end).forEach(i -> {
                checkBits[i] = true;
            });
        }

        //전체 어절에 일치하는 term 이 들어온 경우 우선 선정 필요..?

        CandidateTerms candidateTerms = getCandidateTerms(idx);

        if(candidateTerms == null){
            candidateTerms = new CandidateTerms();
            candidateTerms.add(term);

            setCandidateTerms(idx, candidateTerms);
        }else{
            candidateTerms.add(term);
        }


//        CandidateTerms prevCandidateTerms = getPrevCandidateTerms(end);
//
//        if(prevCandidateTerms == null){
//            prevCandidateTerms = new CandidateTerms();
//            prevCandidateTerms.add(term);
//
//            setPrevCandidateTerms(end, prevCandidateTerms);
//        }else{
//            prevCandidateTerms.add(term);
//        }

        //*** 네이밍 좀 변경하자 헷갈림. 특히 terms

        //1. 기존 입력 된 offset, length 범위 보다 작은 경우 제외 처리 ? -> X
        // 체크 로직 구현 방안
        //2. 사전 단어 일 때
        //   이전 추출 결과 체언 + 현재 체언 케이스에 한해 조합 할지 여부 ->

        //3. 각 offset 에서 가장 긴 term 추출 방안 -> X


        // 구조
        // offset 별 (offset, length) 조합 객체

        //unknown 영역 추출 정보 구성 -> X

    }

    public List<Term> getTerms() {
        return terms;
    }

    public void addTerm(Term term){
        if(term != null) {
            terms.add(term);

            offset += term.getLength();
        }
    }

    public Term getLastTerm(){

        if(terms.size() == 0){
            return null;
        }

        return terms.get(terms.size() - 1);
    }

    public CandidateTerms getCandidateTerms(int idx){
        return candidateTerms[idx];
    }

    private void setCandidateTerms(int idx, CandidateTerms candidateTerms){
        this.candidateTerms[idx] = candidateTerms;
    }

    public List<MissRange> getMissRange(){

        //로직 개선 필요
        List<MissRange> missRanges = new ArrayList<>();

        int offset = -1;
        int length = 0;

        for(int i=0, len=checkBits.length;i<len;i++){

            boolean chk = checkBits[i];

            if(chk){

                if(offset > -1){
                    MissRange missRange = new MissRange(offset, length);
                    missRanges.add(missRange);

                    offset = -1;
                }

            }else{

                if(offset > -1){
                    length += 1;
                }else{
                    offset = i;
                    length = 1;
                }
            }
        }

        if(offset > -1){
            MissRange missRange = new MissRange(offset, length);
            missRanges.add(missRange);
        }


        return missRanges;
    }


    public boolean[] getCheckBits() {
        return checkBits;
    }

    public class MissRange{
        private int offset;
        private int length;

        private MissRange(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        @Override
        public String toString() {
            return "MissRange{" +
                    "offset=" + offset +
                    ", length=" + length +
                    '}';
        }
    }
}
