package daon.analysis.ko.dict.rule.operator;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;

public abstract class AbstractOperator implements Operator {

	private Logger logger = LoggerFactory.getLogger(AbstractOperator.class);

	public KeywordRef merge(String surface, String desc, Keyword... subKeywords ){
		int len = subKeywords.length;
		long[] wordIds = new long[len];
		
		for(int i=0;i<len; i++){
			Keyword word = subKeywords[i];
			wordIds[i] = word.getSeq();
		}
		
		KeywordRef keyword = new KeywordRef(surface, wordIds);
		
		/*
		//operator 결과를 조합 키워드로 추가
		Keyword keyword = new Keyword(surface, "cp"); // 조합 키워드 tag 값 cp 설정 
		keyword.setSeq(0); // 조합 키워드 seq 값 0 설정 
		keyword.setDesc(desc);
		
		List<Keyword> subWords = Arrays.asList(subKeywords);
		keyword.setSubWords(subWords);
		*/
		return keyword;
	}

}
