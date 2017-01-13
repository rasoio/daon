package daon.analysis.ko.dict.rule.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.dict.rule.Merger;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;
import daon.analysis.ko.util.Utils;

/**
 * 서술격 조사 + 어미
 */
public class PredicativeParticleEndingOperator extends AbstractOperator implements Operator {

	private Logger logger = LoggerFactory.getLogger(PredicativeParticleEndingOperator.class);
	
	private char[] ng = new char[]{'ㅇ'};
	
	private char[] eo = new char[]{'ㅓ'};
	
	@Override
	public void grouping(Merger merger, PrevInfo prevInfo) {
		
		merger.addPrevInfo(AlterRules.ShortenYIPP, prevInfo);
	}

	@Override
	public void grouping(Merger merger, NextInfo nextInfo) {
		
		if(isShortenYIPP(nextInfo)){
			merger.addNextInfo(AlterRules.ShortenYIPP, nextInfo);
		}
	}
	
	@Override
	public KeywordRef make(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo) {
		KeywordRef keywordRef = null;
		
		switch (rule)
	    {
	      case ShortenYIPP:
	    	  keywordRef = getShortenYIPP(prevInfo, nextInfo);
	    	  break;
	    }
		
		return keywordRef;
	}
	
	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !                                                                                 !
	 * ! 서술격 조사 '이' 탈락 현상                                                      !
	 * !                                                                                 !
	 * ! 학생이다. : 학생/n* + 이/pp + 다/e*                                             !
	 * ! 사과다.   : 사과/n* + 이/pp + 다/e*                                             !
	 * ! 사과라면  : 사과 (무종성 체언) + 이 + 어미                                      !
	 * !                                                                                 !
	 * ! 왼쪽에 무종성 체언이 오는 경우 '이'를 제거할 수 있다.                           !
	 * ! 그러면, 사과 /n* /pp 다/e* 형태가 되고, 나중에 태그를 모두 없애므로 사라진다.   !
	 * !                                                                                 !
	 * ! '여' 축약 현상 : '이 + 어 = 여'에 대한 변환 (서술격조사/용언) 모두 적용         !  
	 * ! '이' 다음에 '어'가 올 때 발생된다.                                              !
	 * ! 사과이어서, 사과여서 모두 가능.                                                 !
	 * ! 즉 다음에 '어'가 올 때는 선택적으로 '여'로 축약될 수 있다.                      !
	 * ! '이'가 없어지는 것이 아님.                                                      !
	 * !                                                                                 !
	 * ! 사과였다. : 사과/nc + 이/pp + 었/ep + 다/ef => 사과/nc /pp 였/ep 다/ef          !
	 * ! 높였다.   : 높이/vb + 었/ep + 다/ef  => 높였다.                                 !
	 * !          => 높이 [VBYI /vb ]VBYI 었/ep 다/ef                                    !
	 * !          => 높 ㅇ /vb ㅕ ㅆ /ep 다 /ef                                          !
	 * ! 시켰다.   : 시키/xv + 었 + 다   => 시켰다.                                      !
	 * ! 가렸다.   : 가리/vb + 었 + 다   => 가렸다.                                      !
	 * !                                                                                 !
	 * ! '비다'(o), '비었다'(o), '볐다'(x) (자리가 비다 vj)                              !
	 * ! '이다'(o), '이었다'(o), '였다'(x) (짊을 이다)                                   !
	 * ! '지다'(o), '지었다'(o), '졌다'(o) (경기에서 지다)                               !
	 * ! '지다'(o), '지었다'(o), '졌다'(o) (경기에서 성적이 나아졌다)                    !
	 * ! '치다'(o), '치었다'(o), '쳤다'(o) (공을 치다)                                   !
	 * ! '피다'(o), '피었다'(o), '폈다'(o) (돗자리를 폈다)                               !
	 * !                                                                                 !
	 * ! 시키었다. 가리었다. 모두 가능 (본디말, 줄임말 모두 가능)                        !
	 * ! 사과이다. 사과이라면. 모두 가능 (본디말, 줄임라 모두 가능)                      !
	 * !                                                                                 !
	 * ! 서술격 조사 '이'가 탈락하는 것은 선택적이고, 용언의 경우도 마찬가지이다.        !
	 * !                                                                                 !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * ! 서술격 조사 '이' 다음에는 '아'로 시작하는 어미는 올 수 없다.
	 * ! '이' 발음은 음성 모음이므로 '어/아' 중 '어'가 와야 한다.
	 * ! '사과/nc이/pp아야지/ec'
	 * define PPFilter			~$[ %/pp ㅇ ㅏ ] ;
	 * 
	 * ! 서술격 조사 '이' 탈락 현상
	 * ! 사과며 ('사과이며'도 가능)
	 * define PPSyllable		[ ㅇ ㅣ FILLC ] ;
	 * define NotEo			[ [Onset - ㅇ] [Peak - ㅓ] ] ;
	 * define DelPPSyllable	PPSyllable (->) 0 || FILLC NounStringSet _ %/pp NotEo ;
	 * 
	 * ! 서술격 조사 '이'의 축약현상으로 '여'가 될 때 
	 * ! 사과였다 ('사과이었다'도 가능)
	 * define ShortenYIPP		[ㅇ ㅣ FILLC %/pp ㅇ ㅓ] (->) %/pp ㅇ ㅕ || FILLC NounStringSet _ ;
	 * 
	 * ! 용언인데 '이'로 끝난 단어가 '어'와 만날 때 (높였다)
	 * ! 지다(o), 치다(o), 피다(o), 이다 (x), 비다(x)
	 * define ShortenYIVB		[ VerbStringSet -> %[VBYINOT ... %]VBYINOT || .#. [ㅇ | ㅂ] ㅣ FILLC _ ㅇ ㅓ ] .o.
	 * 						[ VerbStringSet (->) %[VBYI ... %]VBYI || Onset ㅣ FILLC _ ㅇ ㅓ ] .o.
	 * 						[ ㅣ FILLC %[VBYI  -> 0 ] .o. [ %]VBYI ㅇ ㅓ -> ㅕ ] .o.
	 * 						[ %[VBYINOT -> 0 ] .o. [ %]VBYINOT -> 0] ;	
	 * 
	 * ! 아녀 : 아니/vn + 어/ef ==> 그냥 아녀/it, 아냐/it (감탄사)로 놓는다.
	 * ! 아뇨 : 아니/vn + 오/ef
	 * ! 아녔다 : 이런 말은 없음. 아니었다. (즉, 아니다는 줄임형이 없다.)
	 * 
	 * ! '이' 탈락 & '이' 축약
	 * define RuleYI			PPFilter .o. DelPPSyllable .o. ShortenYIPP .o. ShortenYIVB ;
	 * 
	 * @param info
	 * @return
	 */
	public KeywordRef getShortenYIPP(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String nextWord = n.getNextWord();

		char[] nextStart = n.getNextStart();

		
		char middle = Utils.compound('ㅇ', 'ㅕ', nextStart[2]);
		
		String str = middle + nextWord.substring(1);

//			logger.info("word : {}, prev : {} ({}), next :{} ({})", str, prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isShortenYIPP(NextInfo info) {

		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, eo, Utils.JONGSEONG)){ // {'ㅇ','ㅓ'}
			
			return true;
		}
		
		return false;
	}
	
}
