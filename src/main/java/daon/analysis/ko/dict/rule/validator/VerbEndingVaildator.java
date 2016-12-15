package daon.analysis.ko.dict.rule.validator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.store.MergeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.dict.config.Config.IrrRule;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;
import daon.analysis.ko.util.Utils;

/**
 * 용언 + 어미 Filter
 */
public class VerbEndingVaildator implements Vaildator{
	
	private Logger logger = LoggerFactory.getLogger(VerbEndingVaildator.class);
	
	private char[] brightVowel = new char[] {'ㅏ', 'ㅗ'};
	
	private char[] darkVowel = Utils.removeElement(Utils.JUNGSEONG, brightVowel); 

	private char[] darkMinusEU = ArrayUtils.removeElement(darkVowel, 'ㅡ'); 
	private char[] jongseong = Utils.removeElement(Utils.JONGSEONG, Utils.NO_JONGSEONG); 
	
	private IrrRule[] irrSet = ArrayUtils.removeElement(Utils.IRR_RULES, IrrRule.irrl);
	private IrrRule[] irrSet2 = ArrayUtils.removeElement(irrSet, IrrRule.irrL);
	
	
	@Override
	public boolean validate(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo) {
		boolean isValidated = true;
		
		Keyword prev = prevInfo.getPrev();
		Keyword next = nextInfo.getNext();
		
		String prevWord = prevInfo.getPrevWord();
		String nextWord = nextInfo.getNextWord();

		char[] prevEnd = prevInfo.getPrevEnd();
		char[] nextStart = nextInfo.getNextStart();

		return isValidated;
		/*
//		!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//		!                                                                                 !
//		! 용언 + 어미 Filter                                                              !
//		!                                                                                 !
//		! 1. 형용사일때는 진행형 어미를 붙일 수 없다. "아릅답 + 는"                       !
//		! 2. 선어말어미 다음에는 '아'로 시작하는 어미가 오지 않는다. "고마웠+아야지'      !
//		!                                                                                 !
//		!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//
//		define VerbEndingFilter	~$[ (IrrSet) %/vj ㄴ ㅡ %_ㄴ ] .o. 
//								~$[ %/ep ㅇ ㅏ ] ;
		//1. 형용사일때는 진행형 어미를 붙일 수 없다. "아릅답 + 는"
//		define VerbEndingFilter	~$[ (IrrSet) %/vj ㄴ ㅡ %_ㄴ ] .o.
		isValidated = verbEndingFilter(info);
		
		if(!isValidated){
			return isValidated;
		}
		
		//나중에 정리할까..?
 
//		~$[ %/ep ㅇ ㅏ ] ;
		
		
		
		
//		define BrightVowel			[ ㅏ | ㅗ ] ;
//		define DarkVowel			[ Peak - BrightVowel ] ;
//		define DarkSyllable			[ ㅇ ㅓ Coda ] ;
//		define BrightSyllable		[ ㅇ ㅏ Coda ] ;
//		define BrightVowelHarmony	~$[ [   [Onset BrightVowel Coda (IrrSet - %/irrl) VerbStringSet DarkSyllable ]
//										  - [[[[ㄷ|ㄲ|ㄱ|ㄴ|ㅁ|ㄸ]ㅏ]|ㄹ[ㅏ|ㅗ]] %_ㅂ %/irrb [%/vb|%/vj] ㅇ ㅓ Coda ]
//										  - [[[ㄱ|ㅈ|ㄴ]ㅏ|[ㅇ|ㅂ] ㅗ] FILLC VerbStringSet ㅇ ㅓ FILLC ] 
//								   	      - [ ㅎ ㅏ FILLC VerbStringSet ㅇ ㅓ Coda] ]
//										| [[[[ㄷ|ㄲ|ㄱ|ㄴ|ㅁ|ㄸ]ㅏ]|ㄹ[ㅏ|ㅗ]] %_ㅂ %/irrb [%/vb|%/vj] ㅇ ㅏ Coda ]
//										| [ ㅎ ㅏ FILLC VerbStringSet ㅇ ㅏ Coda]
//										| [ Onset BrightVowel FILLC ㄹ ㅡ FILLC %/irrl VerbSet DarkSyllable ] ] ;
		
//		[Onset BrightVowel Coda (IrrSet - %/irrl) VerbStringSet DarkSyllable ]
		if(Utils.endWith(prev, Utils.CHOSEONG, brightVowel, Utils.JONGSEONG)
			&& Utils.isTag(prev, POSTag.v)
			&& Utils.isIrrRule(prev, irrSet)
			&& Utils.startsWith(next, new char[]{'ㅇ'}, new char[]{'ㅓ'}, Utils.JONGSEONG)
			){
			
			isValidated = false;
			
//			- [[[[ㄷ|ㄲ|ㄱ|ㄴ|ㅁ|ㄸ]ㅏ]|ㄹ[ㅏ|ㅗ]] %_ㅂ %/irrb [%/vb|%/vj] ㅇ ㅓ Coda ]
			if((Utils.endWith(prev, new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'ㅂ'}) 
				|| Utils.endWith(prev, new char[]{'ㄹ'}, brightVowel, new char[]{'ㅂ'}))
					&& Utils.isTag(prev, new POSTag[] { POSTag.vb, POSTag.vj})
					&& Utils.isIrrRule(prev, IrrRule.irrb)
					&& Utils.startsWith(next, new char[]{'ㅇ'}, new char[]{'ㅓ'}, Utils.JONGSEONG)
					){
				isValidated = true;
			}
			
//			- [[[ㄱ|ㅈ|ㄴ]ㅏ|[ㅇ|ㅂ] ㅗ] FILLC VerbStringSet ㅇ ㅓ FILLC ] 
			if(Utils.endWith(prev, new String[]{"가", "자", "나", "오", "보"})
				&& Utils.startsWith(next, "어")){
				
				isValidated = true;
			}
			
//			- [ ㅎ ㅏ FILLC VerbStringSet ㅇ ㅓ Coda] ]
			if(Utils.endWith(prev, new String[]{"하"})
				&& Utils.startsWith(next, new char[]{'ㅇ'}, new char[]{'ㅓ'}, Utils.JONGSEONG)){
				
				isValidated = true;
			}
			
			if(!isValidated){
//				logger.info("prev : {} ({}), next :{} ({})", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			}
			
		}
		
//		| [[[[ㄷ|ㄲ|ㄱ|ㄴ|ㅁ|ㄸ]ㅏ]|ㄹ[ㅏ|ㅗ]] %_ㅂ %/irrb [%/vb|%/vj] ㅇ ㅏ Coda ]
		if((Utils.endWith(prev, new char[]{'ㄷ','ㄲ','ㄱ','ㄴ','ㅁ','ㄸ'}, new char[]{'ㅏ'}, new char[]{'ㅂ'}) 
			|| Utils.endWith(prev, new char[]{'ㄹ'}, brightVowel, new char[]{'ㅂ'}))
				&& Utils.isTag(prev, new POSTag[] { POSTag.vb, POSTag.vj})
				&& Utils.isIrrRule(prev, IrrRule.irrb)
				&& Utils.startsWith(next, new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)
				){
			
			isValidated = false;
		}
		
//		| [ ㅎ ㅏ FILLC VerbStringSet ㅇ ㅏ Coda]
		if(Utils.endWith(prev, "하") 
			&& Utils.isTag(prev, POSTag.v)
			&& Utils.startsWith(next, new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)
			){
			
			isValidated = false;
		}
		
//		| [ Onset BrightVowel FILLC ㄹ ㅡ FILLC %/irrl VerbSet DarkSyllable ] ]
		if(Utils.isMatch(prevEnd2, Utils.CHOSEONG, brightVowel)
			&& Utils.endWith(prev, "르")
			&& Utils.isTag(prev, POSTag.v)
			&& Utils.isIrrRule(prev, IrrRule.irrl)
			&& Utils.startsWith(next, new char[]{'ㅇ'}, new char[]{'ㅓ'}, Utils.JONGSEONG)
			){
			
//			logger.info("prev : {} ({}), next :{} ({})", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			isValidated = false;
		}
		
		
//		define DarkMinusEU			[ [DarkVowel Coda] - [ㅡ FILLC] ] ;
//		define DarkVowelHarmony		~$[   [ Onset DarkMinusEU (IrrSet - %/irrl - %/irrL) VerbStringSet BrightSyllable ] 
//										| [ Onset DarkVowel FILLC ㄹ ㅡ FILLC %/irrl VerbStringSet BrightSyllable ] 
//										| [ Onset DarkVowel FILLC ㄹ ㅡ FILLC %/irrL VerbStringSet BrightSyllable ] ] ;
//
//		define EUHarmony			~$[   [ Onset BrightVowel Coda Onset ㅡ FILLC VerbStringSet DarkSyllable ]   
//										| [ Onset DarkVowel Coda Onset ㅡ FILLC VerbStringSet BrightSyllable ] ] ;
//
		
//		! 아파 (o), 아퍼 (x), 아팠다 (o), 아펐다 (x) (아프다)                             !
//		! 모아 (o), 모어 (x), 모았다 (o), 모었다 (x) (모으다)                             !
//		! 단음절 '으'의 경우는 '어'가 와야 하는데 (쓰 + 어 => 써)                         !
//		! 2음절 이상일 경우는 바로 전 음절의 모음에 따라 모음 현상이 온다.                !
//		! 아프 + 아/어 => '프' 앞의 음절이 '아' => '으' 탈락 => '아파'   
//		define SingleEUSyllable		[VerbStringSet -> ... %]SINGLEEU || .#. Onset ㅡ FILLC _ BrightSyllable] .o.
//									~$[ %]SINGLEEU ] .o.  [ %]SINGGLEEU -> 0 ] ;
//
//		define EolaRestriction		[[[ㄱ|ㅈ|ㄴ]ㅏ|[ㅇ|ㅂ] ㅗ] FILLC VerbStringSet ㅇ ㅓ FILLC ] => _ ㄹ ㅏ FILLC ;
//
//		define VowelHarmony			BrightVowelHarmony .o. DarkVowelHarmony .o. 
//									EUHarmony .o. SingleEUSyllable .o. EolaRestriction ;
		
//		[ Onset DarkMinusEU (IrrSet - %/irrl - %/irrL) VerbStringSet BrightSyllable ] 
		if(Utils.isMatch(prevEnd, Utils.CHOSEONG, darkMinusEU, jongseong)
			&& Utils.isIrrRule(prev, irrSet2)
			&& Utils.isTag(prev, POSTag.v)
			&& Utils.isMatch(nextStart, new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)){
			
			isValidated = false;
		}
		
//		| [ Onset DarkVowel FILLC ㄹ ㅡ FILLC %/irrl VerbStringSet BrightSyllable ] 
//		| [ Onset DarkVowel FILLC ㄹ ㅡ FILLC %/irrL VerbStringSet BrightSyllable ]
		if(Utils.isMatch(prevEnd2, Utils.CHOSEONG, darkVowel)
			&& Utils.isMatch(prevEnd, new char[]{'ㄹ'}, new char[]{'ㅡ'})
			&& Utils.isIrrRule(prev, new IrrRule[] {IrrRule.irrl, IrrRule.irrL})
			&& Utils.isTag(prev, POSTag.v)
			&& Utils.isMatch(nextStart, new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)){

//			logger.info("prev : {} ({}), next :{} ({})", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			isValidated = false;
		}
		
		
//		[ Onset BrightVowel Coda Onset ㅡ FILLC VerbStringSet DarkSyllable ]
		if(Utils.isMatch(prevEnd2, Utils.CHOSEONG, brightVowel, Utils.JONGSEONG)
			&& Utils.isMatch(prevEnd, Utils.CHOSEONG, new char[]{'ㅡ'})
			&& Utils.isTag(prev, POSTag.v)
			&& Utils.isMatch(nextStart, new char[]{'ㅇ'}, new char[]{'ㅓ'}, Utils.JONGSEONG)){

//			logger.info("prev : {} ({}), next :{} ({})", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			isValidated = false;
		}
		
//		| [ Onset DarkVowel Coda Onset ㅡ FILLC VerbStringSet BrightSyllable ]
		if(Utils.isMatch(prevEnd2, Utils.CHOSEONG, darkVowel, Utils.JONGSEONG)
			&& Utils.isMatch(prevEnd, Utils.CHOSEONG, new char[]{'ㅡ'})
			&& Utils.isTag(prev, POSTag.v)
			&& Utils.isMatch(nextStart, new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)){

//			logger.info("prev : {} ({}), next :{} ({})", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			isValidated = false;
		}
		
		
		

//		[[[ㄱ|ㅈ|ㄴ]ㅏ|[ㅇ|ㅂ] ㅗ] FILLC VerbStringSet ㅇ ㅓ FILLC ] => _ ㄹ ㅏ FILLC ;
		if((Utils.endWith(prev, new char[]{'ㄱ','ㅈ','ㄴ'}, new char[]{'ㅏ'}) 
			|| Utils.endWith(prev, new char[]{'ㅇ', 'ㅂ'}, new char[]{'ㅗ'}))
				&& Utils.isTag(prev, POSTag.v)
				&& Utils.startsWith(next, new char[]{'ㅇ'}, new char[]{'ㅓ'})
				){
			
			char[] c = Utils.getCharAtDecompose(next, 1);
			
			if(c.length > 0){
				if(!Utils.isMatch(c, new char[]{'ㄹ'}, new char[]{'ㅏ'})){
					isValidated = false;
				}
			}
			
//			logger.info("word : {}, prev : {} ({}), next :{} ({})", "", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
		}
		
		
//		define SingleEUSyllable		[VerbStringSet -> ... %]SINGLEEU || .#. Onset ㅡ FILLC _ BrightSyllable] .o.
//		~$[ %]SINGLEEU ] .o.  [ %]SINGGLEEU -> 0 ] ;
		if(Utils.isMatch(prevEnd, Utils.CHOSEONG, new char[]{'ㅡ'})
			&& Utils.isLength(prev, 1)	
			&& Utils.isMatch(nextStart, new char[]{'ㅇ'}, new char[]{'ㅏ'}, Utils.JONGSEONG)){
			
//			logger.info("word : {}, prev : {} ({}), next :{} ({})", "", prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			isValidated = false;
		}
		
		
		
		return isValidated;
		*/
	}


	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !                                                                                 !
	 * ! 용언 + 어미 Filter                                                              !
	 * !                                                                                 !
	 * ! 1. 형용사일때는 진행형 어미를 붙일 수 없다. "아릅답 + 는"                       !
	 * ! 2. 선어말어미 다음에는 '아'로 시작하는 어미가 오지 않는다. "고마웠+아야지'      !
	 * !                                                                                 !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define VerbEndingFilter	~$[ (IrrSet) %/vj ㄴ ㅡ %_ㄴ ] .o. 
	 * 						~$[ %/ep ㅇ ㅏ ] ;
	 * 
	 * @param info
	 * @return
	 */
	/*
	private boolean verbEndingFilter(MergeInfo info) {
		boolean isValidated = true;
		
		
		Keyword prev = info.getPrev();
		Keyword next = info.getNext();
		
		if(Utils.isTag(prev, POSTag.vj)
			&& Utils.isIrrRule(prev)	
			&& Utils.startsWith(next, "는")){
			
			isValidated = false;
		}
		
		return isValidated;
	}
	*/
	
}
