package daon.analysis.ko.dict.rule.validator;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.MergeInfo;

public interface Vaildator {
	
	public boolean validate(MergeInfo info);
}
