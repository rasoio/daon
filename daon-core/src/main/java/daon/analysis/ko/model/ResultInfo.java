package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.util.Utils;
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
    private int wordLength;
    private int offset = 0;

    private final boolean[] checkBits;

    //후보셋
    private final CandidateTerms[] candidateTerms;

    //후보셋
    private List<FilterSet> filteredSet = new ArrayList<>();

    private FilterSet bestFilterSet;

    //최종 결과셋
    private List<Term> terms = new LinkedList<>();

    private ResultInfo(char[] chars, int length) {
        this.chars = chars;
        this.length = length;
        this.wordLength = length;
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

    public int getWordLength() {
        return wordLength;
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

        if(!checkBits[offset] || !checkBits[end - 1]) {
            IntStream.range(offset, end).forEach(i -> {
                checkBits[i] = true;
            });
        }

        // S 태그 이전까지의 매칭은 전체 매칭 처리
        if (end == getLength() && Utils.isTag(term.getFirst().getTag(), POSTag.S)) {
            wordLength = offset;
        }

        //뒷 2음절 매칭 시 뒤 1음절 결과 제거
        if( end == getLength() && length == 2){
            candidateTerms[end-1] = null;
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
    }

    public void merge(){

//        candidateTerms[0].getTerms().remove()
    }


    public void setFilteredSet(List<FilterSet> filteredSet) {
        this.filteredSet = filteredSet;
    }

    public List<FilterSet> getFilteredSet() {
        return filteredSet;
    }

    public FilterSet getBestFilterSet() {
        return bestFilterSet;
    }

    public void setBestFilterSet(FilterSet bestFilterSet) {
        this.bestFilterSet = bestFilterSet;
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
