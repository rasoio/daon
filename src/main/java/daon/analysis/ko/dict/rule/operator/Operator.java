package daon.analysis.ko.dict.rule.operator;

import java.util.List;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.MergeInfo;

public interface Operator {
	
	/**
	 * 조합 결과를 가지고 FST 생성 추가
	 * 조건 부합 시에만 결과에 추가
	 * 
	 * @param prev
	 * @param next
	 * @return 조합 결과, 결과가 없으면 빈 list 객체 반환
	 */
	public List<Keyword> merge(MergeInfo info);
}
