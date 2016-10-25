package daon.analysis.ko.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 분석 결과
 */
public class ResultTerms {

	private Map<Integer, List<Term>> lookupResults;
	
	private int idx = -1;
	
	private List<Term> results = new ArrayList<Term>();
	
	public ResultTerms(Map<Integer, List<Term>> lookupResults) {
		this.lookupResults = lookupResults;
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
