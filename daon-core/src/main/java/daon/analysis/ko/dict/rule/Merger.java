package daon.analysis.ko.dict.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.dict.rule.operator.Operator;
import daon.analysis.ko.dict.rule.validator.Validator;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.MergeSet;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;

public class Merger {
	public String desc;
	
	public Validator validator;
	
	public Operator operator;
	
	private Map<AlterRules,MergeSet> data = new HashMap<AlterRules,MergeSet>();
	
	private final List<Summary> summaries = new ArrayList<Summary>();
	
	private boolean isDebug = false;
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Validator getValidator() {
		return validator;
	}

	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	public Map<AlterRules, MergeSet> getData() {
		return data;
	}
	
	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}
	
	public List<Summary> getSummaries() {
		return summaries;
	}

	public void addPrevList(Keyword keyword){

		if(keyword.getProb() < 15){
		
            if(operator != null){

                PrevInfo info = new PrevInfo(keyword);
                operator.grouping(this, info);
            }
		}
	}
	
	public void addNextList(Keyword keyword){
		if(keyword.getProb() < 20){
			
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
		
		data.entrySet().stream().forEach(e -> {
			
			AlterRules rule = e.getKey();
			MergeSet set = e.getValue();
			
			Summary summary = new Summary(rule);
			
			//통계 정보 입력
			summary.getPrevCnt().set(set.getPrevList().size());
			summary.getNextCnt().set(set.getNextList().size());
			
			if(set.isValid()){
				set.getPrevList().stream().forEach(p -> {
					set.getNextList().stream().forEach(n->{
						
						boolean isValid = true;
						boolean isSuccess = true;
						
						if(validator != null){
							isValid = validator.validate(rule, p, n);
						}
						
						if(isValid){
							isSuccess = operator.execute(rule, p, n, keywordRefs);
						}
						
						//조합 성공 건수 / 실패건수 / validation 실패건수 측정 
						if(isDebug){
							//loop 카운트 측정
							summary.getLoopCnt().incrementAndGet();
	
							if(isSuccess){
								summary.getRefCnt().incrementAndGet();
							}else{
								summary.getFailCnt().incrementAndGet();
							}
							
							if(!isValid){
								summary.getValidFailCnt().incrementAndGet();
							}
						}
						
					});
				});
			}
			
			summaries.add(summary);
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
	
	
	
	public class Summary {
		
		private final AlterRules rule;
		
		private AtomicInteger prevCnt = new AtomicInteger(0);
		
		private AtomicInteger nextCnt = new AtomicInteger(0);
		
		private AtomicInteger loopCnt = new AtomicInteger(0);
		
		private AtomicInteger refCnt = new AtomicInteger(0);
		
		private AtomicInteger failCnt = new AtomicInteger(0);
		
		private AtomicInteger validFailCnt = new AtomicInteger(0);

		public Summary(AlterRules rule) {
			this.rule = rule;
		}

		public AlterRules getRule() {
			return rule;
		}

		public AtomicInteger getPrevCnt() {
			return prevCnt;
		}

		public int getPrevCntInt() {
			return prevCnt.get();
		}

		public void setPrevCnt(AtomicInteger prevCnt) {
			this.prevCnt = prevCnt;
		}

		public AtomicInteger getNextCnt() {
			return nextCnt;
		}

		public int getNextCntInt() {
			return nextCnt.get();
		}

		public void setNextCnt(AtomicInteger nextCnt) {
			this.nextCnt = nextCnt;
		}

		public AtomicInteger getLoopCnt() {
			return loopCnt;
		}

		public int getLoopCntInt() {
			return loopCnt.get();
		}

		public void setLoopCnt(AtomicInteger loopCnt) {
			this.loopCnt = loopCnt;
		}

		public AtomicInteger getRefCnt() {
			return refCnt;
		}

		public int getRefCntInt() {
			return refCnt.get();
		}

		public void setRefCnt(AtomicInteger refCnt) {
			this.refCnt = refCnt;
		}

		public AtomicInteger getFailCnt() {
			return failCnt;
		}

		public void setFailCnt(AtomicInteger failCnt) {
			this.failCnt = failCnt;
		}

		public AtomicInteger getValidFailCnt() {
			return validFailCnt;
		}

		public void setValidFailCnt(AtomicInteger validFailCnt) {
			this.validFailCnt = validFailCnt;
		}

		@Override
		public String toString() {
			return "Summary [rule=" + rule + ", prevCnt=" + prevCnt + ", nextCnt=" + nextCnt + ", loopCnt=" + loopCnt
					+ ", refCnt=" + refCnt + ", failCnt=" + failCnt + ", validFailCnt=" + validFailCnt + "]";
		}

	}
	
}
