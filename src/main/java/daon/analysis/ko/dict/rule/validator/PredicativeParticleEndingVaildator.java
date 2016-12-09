package daon.analysis.ko.dict.rule.validator;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.config.Config.IrrRule;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.MergeInfo;
import daon.analysis.ko.util.Utils;

/**
 * 서술격 조사 '이' + 어미
 */
public class PredicativeParticleEndingVaildator implements Vaildator{
	
	private Logger logger = LoggerFactory.getLogger(PredicativeParticleEndingVaildator.class);
	
	@Override
	public boolean validate(MergeInfo info) {
		boolean isValidated = true;
		
//		~$[ %/pp ㅇ ㅏ ] ;
		
		//선어말어미 다음에는 '아'로 시작하는 어미가 오지 않는다. "고마웠+아야지'   
		if(Utils.isTag(info.getPrev(), POSTag.pp)
			&& Utils.startsWith(info.getNext(), new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)){
			
//			logger.info("prev : {} ({}), next :{} ({})", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			isValidated = false;
		}
		
		
		return isValidated;
	}
	
}
