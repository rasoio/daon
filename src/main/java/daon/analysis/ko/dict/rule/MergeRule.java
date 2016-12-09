package daon.analysis.ko.dict.rule;

import java.util.ArrayList;
import java.util.List;

import daon.analysis.ko.dict.rule.operator.Operator;
import daon.analysis.ko.dict.rule.validator.Vaildator;
import daon.analysis.ko.model.Keyword;

public class MergeRule {
	public String desc;
	
	public List<Vaildator> validators;
	
	public List<Operator> operators;
	
	private List<Keyword> prevList = new ArrayList<Keyword>();
	
	private List<Keyword> nextList = new ArrayList<Keyword>();
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Vaildator> getValidators() {
		return validators;
	}

	public void setValidators(List<Vaildator> validators) {
		this.validators = validators;
	}

	public List<Operator> getOperators() {
		return operators;
	}

	public void setOperators(List<Operator> operators) {
		this.operators = operators;
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
