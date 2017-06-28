package daon.analysis.ko.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mac on 2017. 6. 27..
 */
public class CandidateTerms {
//    private List<Term> terms = new ArrayList<>();
    private Set<Term> terms = new HashSet<>();

    private Term longestTerm; // 의미 없음 제거...

    /**
     *
     * @param term offset 은 동일한 term
     */
    public void add(Term term){
//            terms.add(term);

        if(longestTerm == null){
            longestTerm = term;
        }

        // 가장 긴 매칭 결과보다 작은 결과는 제외
        if(longestTerm.getLength() <= term.getLength()){
            terms.add(term);

            longestTerm = term;
        }

    }

    public Set<Term> getTerms() {
        return terms;
    }

    public Term getLongestTerm() {
        return longestTerm;
    }

    @Override
    public String toString() {
        return "CandidateTerms{" +
                "terms=" + terms +
                '}';
    }
}
