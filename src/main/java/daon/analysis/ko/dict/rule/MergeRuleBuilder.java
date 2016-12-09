package daon.analysis.ko.dict.rule;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.rule.operator.Operator;
import daon.analysis.ko.dict.rule.validator.Vaildator;

public class MergeRuleBuilder {
	
	private Logger logger = LoggerFactory.getLogger(MergeRuleBuilder.class);
	
	public String desc;
	
	public List<Vaildator> validators = new ArrayList<Vaildator>();
	
	public List<Operator> operators = new ArrayList<Operator>();

	public static MergeRuleBuilder create() {
		return new MergeRuleBuilder();
	}

	private MergeRuleBuilder() {}

	public MergeRuleBuilder setDesc(String desc){
		this.desc = desc;
		return this;
	}
	
	public MergeRuleBuilder add(Vaildator validator){
		validators.add(validator);
		return this;
	}

	public MergeRuleBuilder add(Operator operator){
		operators.add(operator);
		return this;
	}
	
	public MergeRule build() {
		MergeRule rule = new MergeRule();
		
		rule.setDesc(desc);
		rule.setValidators(validators);
		rule.setOperators(operators);
		
		return rule;
	}
	
}
