package daon.analysis.ko.dict.rule.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;
import daon.analysis.ko.util.Utils;

/**
 * 서술격 조사 '이' + 어미
 */
public class PredicativeParticleEndingVaildator implements Vaildator{
	
	private Logger logger = LoggerFactory.getLogger(PredicativeParticleEndingVaildator.class);
	
	@Override
	public boolean validate(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo) {
		boolean isValidated = true;
		
		Keyword prev = prevInfo.getPrev();
		Keyword next = nextInfo.getNext();
		
		String prevWord = prevInfo.getPrevWord();
		String nextWord = nextInfo.getNextWord();

		char[] prevEnd = prevInfo.getPrevEnd();
		char[] nextStart = nextInfo.getNextStart();
		
//		~$[ %/pp ㅇ ㅏ ] ;
		
		//선어말어미 다음에는 '아'로 시작하는 어미가 오지 않는다. "고마웠+아야지'   
		if(Utils.isTag(prev, POSTag.pp)
			&& Utils.isMatch(nextStart, new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)){
			
//			logger.info("prev : {} ({}), next :{} ({})", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			isValidated = false;
		}
		
		
		return isValidated;
	}
	
}
