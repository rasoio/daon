package daon.analysis.ko.model;

import java.util.ArrayList;
import java.util.List;

import daon.analysis.ko.dict.rule.MergeRule;

public class MergeSet {
	
	private MergeRule rule;
	
	private String description;
	
	private List<Keyword> prevList = new ArrayList<Keyword>();
	
	private List<Keyword> nextList = new ArrayList<Keyword>();
	
	//조합 시 제약 사항 정의 필요
	public MergeSet(MergeRule rule, String description) {
		this.rule = rule;
		this.description = description;
	}

	public MergeRule getRule() {
		return rule;
	}

	public void setRule(MergeRule rule) {
		this.rule = rule;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Keyword> getPrevList() {
		return prevList;
	}

	public void setPrevList(List<Keyword> prevList) {
		this.prevList = prevList;
	}

	public List<Keyword> getNextList() {
		return nextList;
	}

	public void setNextList(List<Keyword> nextList) {
		this.nextList = nextList;
	}
	
	public void addPrevList(Keyword keyword){
		this.prevList.add(keyword);
	}
	
	public void addNextList(Keyword keyword){
		this.nextList.add(keyword);
	}
	
}
