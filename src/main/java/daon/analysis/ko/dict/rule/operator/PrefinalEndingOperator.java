package daon.analysis.ko.dict.rule.operator;

import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.apache.lucene.util.automaton.RegExp;
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
 * 선어말 어미 + 어미 
 */
public class PrefinalEndingOperator extends AbstractOperator implements Operator {

	private Logger logger = LoggerFactory.getLogger(PrefinalEndingOperator.class);
	
	private Automaton eu4 = new RegExp("[ㄹ|ㅁ]").toAutomaton();
	private CharacterRunAutomaton eu4r = new CharacterRunAutomaton(eu4);
	
	private char[] ss = new char[]{'ㅆ'};
	private char[] r = new char[]{'ㄹ'};

	@Override
	public void grouping(Merger merger, PrevInfo prevInfo) {
		
		if(isInsertEU(prevInfo)){
			merger.addPrevInfo(AlterRules.InsertEU, prevInfo);
		}
	}
	
	@Override
	public void grouping(Merger merger, NextInfo nextInfo) {
		
		if(isInsertEU(nextInfo)){
			merger.addNextInfo(AlterRules.InsertEU, nextInfo);
		}
	}
	
	@Override
	public KeywordRef make(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo) {
		KeywordRef keywordRef = null;
		
		switch (rule)
	    {
	      case InsertEU:
	    	  keywordRef = getInsertEU(prevInfo, nextInfo);
	    	  break;
	    }
		
		return keywordRef;
	}
	
	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 매개모음 '으'의 삽입 현상
	 * !
	 * ! 'ㄹ'이 아닌 받침 있는 용언이 '니, ㄹ, 오, 시, ㅁ' 앞에서 '으' 가 삽입됨.
	 * ! 예) 잡+ㄴ=> 잡은, 잡+ㄹ=> 잡을, 잡+오=>잡으오, 잡+시+고=>잡으시고
	 * !     잡+ㅁ=> 잡음, 잡+세요(시어요)=> 잡으세요
	 * !
	 * ! 먹으면   => 먹/vb + 으 면/ec
	 * ! 먹으셔서 => 먹/vb + 으 시/ep 서/ec
	 * ! 먹은     => 먹/vb + 으 ㄴ/ed
	 * ! 먹으니까 => 먹/vb + 으 니까/ec
	 * ! 먹나     => 먹/vb + 나/ec
	 * ! 먹냐며   => 먹/vb + 냐며/ec
	 * ! 먹는다   => 먹/vb + 는다/ef ('는'의 경우는 '으'가 삽입 안됨)
	 * ! 먹는     => 먹/vb + 는/ed
	 * !
	 * ! 도우면   => 돕/vb + 으 면/ed  (나중에 ㅂ 불규칙 처리해야 함)
	 * !
	 * ! 갈 + 셔서 => 가 셔서 (ㄹ 탈락)
	 * !
	 * ! 하였었음을, 하였으니, 하였을, 하겠을, 하겠으리라고, 하겠음. 하겠니, 하신
	 * !
	 * ! 유종성 선어말어미 : 했음을, 했을, 했으리라고, 계셨으리라고 (%_ㅁ, %_ㄹ, ㄹ)
	 * ! 무종성 선어말어미 : 하신, 하심, 하실, 하시리라고 (아무 변화 없음)
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * ! 선어말 어미 다음에 'ㅏ'로 시작하는 어미가 오지 않는다.
	 * ! 했었어야지 (o), 했었아야지 (x)
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
	 * 
	 * define InsertEU1		[..] -> [ㅇ ㅡ FILLC] || [Coda - FILLC - %_ㄹ] (IrrSet) 
	 * 						VerbStringSet _ [ㄴ ㅣ | ㄹ | ㅁ | ㅅ ㅣ | ㅇ ㅗ ] ; 
	 * define InsertEU2       	[..] -> [ㅇ ㅡ] || [Coda - FILLC - %_ㄹ] (IrrSet) 
	 * 						VerbStringSet _ [%_ㄴ | %_ㄹ | %_ㅁ] ;
	 * define InsertEU3		[..] -> [ㅇ ㅡ FILLC] || %_ㅅㅅ %/ep _ ㄹ ; 
	 * define InsertEU4       	[..] -> [ㅇ ㅡ] || %_ㅅㅅ %/ep _ [%_ㄹ | %_ㅁ] ;
	 * define InsertEU			InsertEU1 .o. InsertEU2 .o. InsertEU3 .o. InsertEU4 ;
	 * 
	 * @param info
	 * @return
	 */
	public KeywordRef getInsertEU(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] nextStart = n.getNextStart();
		
		
		if(nextStart.length == 1){
			
			char middle = Utils.compound('ㅇ', 'ㅡ', nextStart[0]);
			
			String str = prevWord + middle + nextWord.substring(1);
			
			keyword = createKeywordRef(str, prev, next);
			
		}else{
			//매개모음 '으' 추가여부 결정 필요
			String str = prevWord + "으" + nextWord;
			
			keyword = createKeywordRef(str, prev, next);
			
		}
		
		return keyword;
	}
	
	public boolean isInsertEU(PrevInfo info) {
		
		char[] prevEnd = info.getPrevEnd();
		
		if(Utils.isMatch(prevEnd, ss, 2)){ //{'ㅆ'}
			return true;
		}
		
		return false;
	}
	
	public boolean isInsertEU(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, r, 0) || eu4r.run(nextStart, 0, nextStart.length)){ // {'ㄹ'}, "ㄹ|ㅁ"
			
			return true;
		}
		
		return false;
	}
	
}
