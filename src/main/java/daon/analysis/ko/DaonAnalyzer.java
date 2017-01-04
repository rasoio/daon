package daon.analysis.ko;

import java.io.IOException;
import java.util.List;

import daon.analysis.ko.dict.connect.ConnectMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;

public class DaonAnalyzer {

	private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

	private Dictionary dictionary;
	
	public DaonAnalyzer(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public ResultTerms analyze(String text) throws IOException{
		//원본 문자
		char[] texts = text.toCharArray();
		
		//총 길이
		int textLength = text.length();
		
		List<Term> terms = dictionary.lookup(texts, 0, textLength);

		ResultTerms results = new ResultTerms(terms);
		
		return results;
		
	}
}
