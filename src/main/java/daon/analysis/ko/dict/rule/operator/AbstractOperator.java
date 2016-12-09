package daon.analysis.ko.dict.rule.operator;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.model.Keyword;

public abstract class AbstractOperator implements Operator {

	private Logger logger = LoggerFactory.getLogger(AbstractOperator.class);

	public Keyword merge(String surface, String desc, Keyword... subKeywords ){
		//operator 결과를 조합 키워드로 추가
		Keyword keyword = new Keyword(surface, "cp"); // 조합 키워드 tag 값 cp 설정 
		keyword.setSeq(0); // 조합 키워드 seq 값 0 설정 
		keyword.setDesc(desc);
		
		List<Keyword> subWords = Arrays.asList(subKeywords);
		keyword.setSubWords(subWords);
		
		return keyword;
	}

}
