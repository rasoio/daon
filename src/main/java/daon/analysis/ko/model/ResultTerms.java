package daon.analysis.ko.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 분석 결과
 */
public class ResultTerms {

	private List<Term> results = new ArrayList<Term>();
	
	public ResultTerms(List<Term> results) {
		this.results = results;
	}

	public List<Term> getResults() {
		return results;
	}

	public void setResults(List<Term> results) {
		this.results = results;
	}
	
}
