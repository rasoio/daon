package daon.analysis.ko.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 분석 결과
 */
public class ResultTerms {

	private Map<Integer, List<Term>> resultsMap = new HashMap<Integer, List<Term>>();

	private List<Term> results = new ArrayList<Term>();
	
	public ResultTerms() {}

	public List<Term> getResults() {
		return results;
	}

	public void setResults(List<Term> results) {
		this.results = results;
	}
	
}
