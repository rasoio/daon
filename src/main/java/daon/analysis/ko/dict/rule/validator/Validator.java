package daon.analysis.ko.dict.rule.validator;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;

public interface Validator {
	
	public boolean validate(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo);
}
