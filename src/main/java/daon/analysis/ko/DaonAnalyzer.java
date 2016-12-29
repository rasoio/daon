package daon.analysis.ko;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.config.Config.CharType;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.tag.Tag;
import daon.analysis.ko.util.CharTypeChecker;

public class DaonAnalyzer {

	private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

	private Dictionary dictionary;
	
	private Tag tag;
	
	public DaonAnalyzer(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public ResultTerms analyze(String text) throws IOException{
		//원본 문자
		char[] texts = text.toCharArray();
		
		//총 길이
		int textLength = text.length();
		
		List<Term> terms = dictionary.lookupImprove(texts, 0, textLength);

		ResultTerms results = new ResultTerms(null);
		results.setResults(terms);
		
		return results;
		
	}
}
