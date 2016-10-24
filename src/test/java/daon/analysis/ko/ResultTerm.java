package daon.analysis.ko;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 분석 결과
 */
public class ResultTerm {

	private Map<Integer, List<Term>> idxResults;
	
	private int idx = -1;
	
	private List<Term> results = new ArrayList<Term>();
	
	public ResultTerm(Map<Integer, List<Term>> idxResults) {
		this.idxResults = idxResults;
	}

	public void add(Term term){

		results.add(term);
		
		idx++;
	}
	
	public Term getPrevTerm(){
		if(idx == -1){
			return null;
		}
		
		return results.get(idx);
	}

	public List<Term> getResults() {
		return results;
	}

	public void setResults(List<Term> results) {
		this.results = results;
	}
	
	
	
}
