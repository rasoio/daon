package daon.analysis.ko.dict.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.dict.rule.operator.Operator;
import daon.analysis.ko.dict.rule.validator.Vaildator;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.MergeSet;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;

public class Merger {
	public String desc;
	
	public Vaildator validator;
	
	public Operator operator;
	
	private Map<AlterRules,MergeSet> data = new HashMap<AlterRules,MergeSet>();
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Vaildator getValidator() {
		return validator;
	}

	public void setValidator(Vaildator validator) {
		this.validator = validator;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	public void addPrevList(Keyword keyword){

//		if(keyword.getTf() > 1){
			
			if(operator != null){
	
				PrevInfo info = new PrevInfo(keyword);
				
				operator.grouping(this, info);
			}
//		}
	}
	
	public void addNextList(Keyword keyword){
		if(keyword.getTf() > 1){
			
			if(operator != null){
				
				NextInfo info = new NextInfo(keyword);
				
				operator.grouping(this, info);
			}

		}
	}

	/**
	 * 조합 실행 
	 * @param keywordRefs
	 */
	public void merge(List<KeywordRef> keywordRefs){
		
//		Map<AlternationRules,Summary> summary;
		
		data.entrySet().stream().forEach(e -> {
			
			AlterRules rule = e.getKey();
			
			//통계 정보 입력
			
			MergeSet set = e.getValue();
			
			if(set.isValid()){
				set.getPrevList().stream().forEach(p -> {
					set.getNextList().stream().forEach(n->{
						
						//loop 카운트 측정
						
						boolean isValid = true;
						if(validator != null){
							isValid = validator.validate(rule, p, n);
						}
						
						if(isValid){
							boolean isSuccess = operator.execute(rule, p, n, keywordRefs);
						}
						
						//조합 성공 건수 / 실패건수 / validation 실패건수 측정 
					});
				});
			}
		});
	}
	
	
	public void addPrevInfo(AlterRules rule, PrevInfo prev){
		
		MergeSet set = data.get(rule);
		
		if(set == null){
			set = new MergeSet(rule);
		}
		
		set.addPrev(prev);
		
		data.put(rule, set);
	}

	public void addNextInfo(AlterRules rule, NextInfo next){
		
		MergeSet set = data.get(rule);
		
		if(set == null){
			set = new MergeSet(rule);
		}
		
		set.addNext(next);
		
		data.put(rule, set);
	}

	public Map<AlterRules, MergeSet> getData() {
		return data;
	}
	
}
