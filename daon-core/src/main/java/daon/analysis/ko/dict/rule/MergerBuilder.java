package daon.analysis.ko.dict.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.rule.operator.Operator;
import daon.analysis.ko.dict.rule.validator.Validator;

public class MergerBuilder {

    private Logger logger = LoggerFactory.getLogger(MergerBuilder.class);

    public String desc;

    public Validator validator;

    public Operator operator;

    private boolean isDebug = false;

    public static MergerBuilder create() {
        return new MergerBuilder();
    }

    private MergerBuilder() {
    }

    public MergerBuilder setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public MergerBuilder setValidator(Validator validator) {
        this.validator = validator;
        return this;
    }

    public MergerBuilder setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public MergerBuilder setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        return this;
    }

    public Merger build() {
        Merger rule = new Merger();

        rule.setDesc(desc);
        rule.setValidator(validator);
        rule.setOperator(operator);
        rule.setDebug(isDebug);

        return rule;
    }

}
