package daon.analysis.ko.dict.rule.operator;

import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.AlterRules;
import daon.analysis.ko.dict.config.Config.IrrRule;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.rule.Merger;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.NextInfo;
import daon.analysis.ko.model.PrevInfo;
import daon.analysis.ko.util.Utils;

/**
 * 용언 + 어미
 */
public class VerbEndingOperator extends AbstractOperator implements Operator {

	
	private Logger logger = LoggerFactory.getLogger(VerbEndingOperator.class);
	
	private char[] jongseong = Utils.removeElement(Utils.JONGSEONG, new char[]{Utils.EMPTY_JONGSEONG, 'ㄹ'});
	
	private Automaton eu1 = new RegExp("(ㄴㅣ|ㄹ|ㅁ|ㅅㅣ|ㅇㅗ).+").toAutomaton();
	private CharacterRunAutomaton eu1r = new CharacterRunAutomaton(eu1);

	private Automaton eu2 = new RegExp("[ㄴ|ㄹ|ㅁ]").toAutomaton();
	private CharacterRunAutomaton eu2r = new CharacterRunAutomaton(eu2);
	
	
	private Automaton dropL1 = new RegExp("(ㄴ|ㄹ|ㅂ)").toAutomaton();
	private Automaton dropL2 = new RegExp("(ㄴ|ㅅㅣ|ㅇㅗ|ㅅㅔ).+").toAutomaton();
	
	private CharacterRunAutomaton dropLr = new CharacterRunAutomaton(Operations.union(dropL1, dropL2));
	
	private Automaton irrGeola = new RegExp("((ㄱ|ㅈ|ㄴ)ㅏ|ㅂㅗ)" + Utils.EMPTY_JONGSEONG + "|ㅇㅣㅆ").toAutomaton();
	private CharacterRunAutomaton irrGeolar = new CharacterRunAutomaton(irrGeola);
	
	private Automaton irrh = new RegExp("[ㄴ|ㄹ|ㅁ|ㅂ]").toAutomaton();
	
	private CharacterRunAutomaton irrhr = new CharacterRunAutomaton(irrh);
	

	private Automaton coda = new RegExp("[ㄴ|ㄹ|ㅁ|ㅂ|ㅇ|ㅅ]").toAutomaton();
	private CharacterRunAutomaton codar = new CharacterRunAutomaton(coda);
	
	/**
	 * 조건 자모 (성능 이슈로 정의해서 사용)
	 * 
	 * 모 음	자 음
		한글	영문	한글	영문	한글	영문	한글	영문
		ㅏ	a	ㅕ	yeo	ㄱ	g,k	ㅌ	t
		ㅓ	eo	ㅛ	yo	ㄴ	n	ㅍ	p
		ㅗ	o	ㅠ	yu	ㄷ	d,t	ㅎ	h
		ㅜ	u	ㅒ	yae	ㄹ	r,l	ㄲ	kk
		ㅡ	eu	ㅖ	ye	ㅁ	m	ㄸ	tt
		ㅣ	i	ㅘ	wa	ㅂ	b,p	ㅃ	pp
		ㅐ	ae	ㅙ	wae	ㅅ	s	ㅆ	ss
		ㅔ	e	ㅝ	wo	ㅇ	ng	ㅉ	jj
		ㅚ	oe	ㅞ	we	ㅈ	j		
		ㅟ	wi	ㅢ	ui	ㅊ	ch		
		ㅑ	ya			ㅋ	k		
	 */
	private char[] ng = new char[]{'ㅇ'};
	
	private char[] eae = new char[]{'ㅔ','ㅐ'};
	
	private char[] eo = new char[]{'ㅓ'};
	private char[] iouoe = new char[]{'ㅣ','ㅗ','ㅜ','ㅚ'};
	private char[] eoayeo = new char[]{'ㅓ','ㅏ','ㅕ'};
	private char[] a = new char[]{'ㅏ'};
	private char[] i = new char[]{'ㅣ'};
	private char[] o = new char[]{'ㅗ'};
	private char[] u = new char[]{'ㅜ'};
	private char[] oe = new char[]{'ㅚ'};
	private char[] ya = new char[]{'ㅑ'};
	private char[] yeo = new char[]{'ㅕ'};
	private char[] eu = new char[]{'ㅡ'};
	private char[] r = new char[]{'ㄹ'};
	private char[] aeo = new char[]{'ㅏ','ㅓ'};
	private char[] nrm = new char[]{'ㄴ', 'ㄹ', 'ㅁ'};
	

	@Override
	public void grouping(Merger merger, PrevInfo prevInfo) {

		if(isIrrConjl(prevInfo)){
			merger.addPrevInfo(AlterRules.IrrConjl, prevInfo);
		}
		
		if(isIrrConjYEO(prevInfo)){
			merger.addPrevInfo(AlterRules.IrrConjYEO, prevInfo);
		}
		
		if(isDropEU(prevInfo)){
			merger.addPrevInfo(AlterRules.DropEU, prevInfo);
		}
		
		if(isInsertEU(prevInfo)){
			merger.addPrevInfo(AlterRules.InsertEU, prevInfo);
		}
		
		if(isDropL(prevInfo)){
			merger.addPrevInfo(AlterRules.DropL, prevInfo);
		}
		
		if(isDropS(prevInfo)){
			merger.addPrevInfo(AlterRules.DropS, prevInfo);
		}
		
		if(isIrrConjD(prevInfo)){
			merger.addPrevInfo(AlterRules.IrrConjD, prevInfo);
		}
		
		if(isIrrConjB(prevInfo)){
			merger.addPrevInfo(AlterRules.IrrConjB, prevInfo);
		}
		
		if(isIrrConjL(prevInfo)){
			merger.addPrevInfo(AlterRules.IrrConjL, prevInfo);
		}
		
		if(isIrrEola(prevInfo)){
			merger.addPrevInfo(AlterRules.IrrEola, prevInfo);
		}
		
		if(isIrrConjH(prevInfo)){
			merger.addPrevInfo(AlterRules.IrrConjH, prevInfo);
		}
		
		if(isConjDiph(prevInfo)){
			merger.addPrevInfo(AlterRules.ConjDiph, prevInfo);
		}
		
		if(isConjEAE(prevInfo)){
			merger.addPrevInfo(AlterRules.ConjEAE, prevInfo);
		}
		
		if(isChangeNullCoda(prevInfo)){
			merger.addPrevInfo(AlterRules.ChangeNullCoda, prevInfo);
		}
	}
	

	@Override
	public void grouping(Merger merger, NextInfo nextInfo) {

		if(isIrrConjl(nextInfo)){
			merger.addNextInfo(AlterRules.IrrConjl, nextInfo);
		}
		
		if(isIrrConjYEO(nextInfo)){
			merger.addNextInfo(AlterRules.IrrConjYEO, nextInfo);
		}
		
		if(isDropEU(nextInfo)){
			merger.addNextInfo(AlterRules.DropEU, nextInfo);
		}
		
		if(isInsertEU(nextInfo)){
			merger.addNextInfo(AlterRules.InsertEU, nextInfo);
		}
		
		if(isDropL(nextInfo)){
			merger.addNextInfo(AlterRules.DropL, nextInfo);
		}
		
		if(isDropS(nextInfo)){
			merger.addNextInfo(AlterRules.DropS, nextInfo);
		}
		
		if(isIrrConjD(nextInfo)){
			merger.addNextInfo(AlterRules.IrrConjD, nextInfo);
		}
		
		if(isIrrConjB(nextInfo)){
			merger.addNextInfo(AlterRules.IrrConjB, nextInfo);
		}
		
		if(isIrrConjL(nextInfo)){
			merger.addNextInfo(AlterRules.IrrConjL, nextInfo);
		}
		
		if(isIrrEola(nextInfo)){
			merger.addNextInfo(AlterRules.IrrEola, nextInfo);
		}
		
		if(isIrrConjH(nextInfo)){
			merger.addNextInfo(AlterRules.IrrConjH, nextInfo);
		}
		
		if(isConjDiph(nextInfo)){
			merger.addNextInfo(AlterRules.ConjDiph, nextInfo);
		}
		
		if(isConjEAE(nextInfo)){
			merger.addNextInfo(AlterRules.ConjEAE, nextInfo);
		}
		
		if(isChangeNullCoda(nextInfo)){
			merger.addNextInfo(AlterRules.ChangeNullCoda, nextInfo);
		}
	}
	

	@Override
	public KeywordRef make(AlterRules rule, PrevInfo prevInfo, NextInfo nextInfo) {
		
		KeywordRef keywordRef = null;
		
		switch (rule)
	    {
	      case IrrConjl:
	    	  keywordRef = getIrrConjl(prevInfo, nextInfo);
	    	  break;
	      case IrrConjYEO:
	    	  keywordRef = getIrrConjYEO(prevInfo, nextInfo);
	    	  break;
	      case DropEU:
	    	  keywordRef = getDropEU(prevInfo, nextInfo);
	    	  break;
	      case InsertEU:
	    	  keywordRef = getInsertEU(prevInfo, nextInfo);
	    	  break;
	      case DropL:
	    	  keywordRef = getDropL(prevInfo, nextInfo);
	    	  break;
	      case DropS:
	    	  keywordRef = getDropS(prevInfo, nextInfo);
	    	  break;
	      case IrrConjD:
	    	  keywordRef = getIrrConjD(prevInfo, nextInfo);
	    	  break;
	      case IrrConjB:
	    	  keywordRef = getIrrConjB(prevInfo, nextInfo);
	    	  break;
	      case IrrConjL:
	    	  keywordRef = getIrrConjL(prevInfo, nextInfo);
	    	  break;
	      case IrrEola:
	    	  keywordRef = getIrrEola(prevInfo, nextInfo);
	    	  break;
	      case IrrConjH:
	    	  keywordRef = getIrrConjH(prevInfo, nextInfo);
	    	  break;
	      case ConjDiph:
	    	  keywordRef = getConjDiph(prevInfo, nextInfo);
	    	  break;
	      case ConjEAE:
	    	  keywordRef = getConjEAE(prevInfo, nextInfo);
	    	  break;
	      case ChangeNullCoda:
	    	  keywordRef = getChangeNullCoda(prevInfo, nextInfo);
	    	  break;
	    }
		
		return keywordRef;
	}
	
	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 종성 탈락현상으로 인해 종성이 사라졌을 때 무음가 종성을 삽입한다.
	 * !
	 * ! 알/vb + 니까/ef
	 * ! ㅇ ㅏ     /vb + ㄴ ㅣ %_ ㄲ ㅏ %_ /ef => ㅇ ㅏ %_ ㄴ ㅣ %_ ㄲ ㅏ %_
	 * !
	 * ! 긋/vb + 어/ef
	 * ! ㄱ ㅡ  /irrs /vb + ㅇ ㅓ %_ /ef => ㄱ ㅡ %_ ㅇ ㅓ %_
	 * !
	 * ! 가/vb + ㄴ다/ef
	 * ! ㄱ ㅏ %_ /vb + %_ㄴ ㄷ ㅏ %_ /ef
	 * !
	 * ! 그걸 : 그거/nm + _ㄹ/po
	 * ! 무종성 체언에 _ㄹ이 붙는다. 이 때 앞 종성 Filler는 없앤다.
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define ChangeNullCoda	[..] -> FILLC || Peak _ (IrrSet) VerbStringSet Onset .o. 
	 * 						FILLC -> 0 || Peak _ (IrrSet) VerbStringSet [%_ㄴ | %_ㄹ | %_ㅁ | %_ㅂ | %_ㅇ | %_ㅅ] .o. 
	 * 						FILLC -> 0 || Peak _ [NounSet | %/ec | %/ef | %/pa | %/pc | %/pq | %/px | %/ps ] 
	 * 						           [%_ㄴ | %_ㄹ] (Syllable) [ %/po | %/px | %/pa ] ;
	 * 
	 * @param info
	 */
	public KeywordRef getChangeNullCoda(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		char[] nextStart = n.getNextStart();
		
		char middle = Utils.compound(prevEnd[0], prevEnd[1], nextStart[0]);
		
		String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord.substring(1);
		
//		logger.info("word : {}, prev : {}, next :{}", str, prev, next);
		
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isChangeNullCoda(PrevInfo info) {
		
		char[] prevEnd = info.getPrevEnd();
		
		if(Utils.isMatch(prevEnd, Utils.NO_JONGSEONG, 2)){ // 종성이 없고
			return true;
		}
		
		return false;
	}
	
	public boolean isChangeNullCoda(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(codar.run(nextStart, 0, nextStart.length)){ // "ㄴ","ㄹ","ㅁ","ㅂ","ㅇ","ㅅ"
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 어미가 '애'/'에'로 끝나는 용언 뒤에 '어'가 올 때 '어'의 탈락 현상
	 * !
	 * ! 본디말/줄임말 모두 된다. 깨, 깨어, 개었다. 갰다. 메어, 메.
	 * !
	 * ! 베어도(o), 베어(o), 베(o) (베+어)
	 * ! 개어도(o), 개서(o), 개(o) (개+어)
	 * !
	 * ! 깨      -> 깨/vb + 어/ec
	 * ! 갰다    -> 개/vb + 었/ep + 다/ef
	 * ! 메      -> 메/vb + 어/ec
	 * !
	 * ! 깨  : ㄲ ㅐ %_ /vb + ㅇ ㅓ %_ㅅㅅ/ep 
	 * !       ㄲ       /vb +    ㅐ %_ㅅㅅ/ep
	 * ! 메  : ㅁ ㅔ %_ /vb + ㅇ ㅓ %_ㅅㅅ/ep
	 * !       ㅁ       /vb +    ㅔ %_ㅅㅅ/ep
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define ConjEAE	ㅔ FILLC %/vb ㅇ ㅓ (->) %/vb ㅔ , ㅐ FILLC %/vb ㅇ ㅓ (->) %/vb ㅐ ,
	 * 				ㅔ FILLC %/vj ㅇ ㅓ (->) %/vj ㅔ , ㅐ FILLC %/vj ㅇ ㅓ (->) %/vj ㅐ ,
	 * 				ㅔ FILLC %/vx ㅇ ㅓ (->) %/vx ㅔ , ㅐ FILLC %/vx ㅇ ㅓ (->) %/vx ㅐ ;
	 * 
	 * @param info
	 */
	public KeywordRef getConjEAE(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		char[] nextStart = n.getNextStart();
		
		char middle = Utils.compound(prevEnd[0], prevEnd[1], nextStart[2]);
		
		String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord.substring(1);
		
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isConjEAE(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		char[] prevEnd = info.getPrevEnd();
		
		if(Utils.isMatch(prevEnd, eae, 1) // 'ㅔ','ㅐ'
			&& Utils.isTag(prev, POSTag.v)){
			
			return true;
		}
		
		return false;
	}
	
	public boolean isConjEAE(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, eo, Utils.JONGSEONG)){ // 'ㅇ', 'ㅓ'
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 이중모음 법칙
	 * !
	 * !    ① ㅣ+ㅓ→ ㅕ   이+어→ 여   가리+어 → 가려 (RuleYI에서 처리됨)
	 * !    ② ㅗ+ㅏ→ ㅘ   오+아→ 와   오+아서 → 와서  
	 * !    ③ ㅜ+ㅓ→ ㅝ   우+어→ 워   두+었다 → 뒀다
	 * !    ④ ㅚ+ㅓ→ ㅙ   외+어→ 왜   되+어   → 돼
	 * !    ⑤ ㅏ+ㅕ→ ㅐ   아+여→ 애   하+여   → 해
	 * !
	 * ! 와   : ㅇ ㅗ %_ /vb + ㅇ ㅏ %_ /ef => ㅇ /vb + ㅘ/ef
	 * ! 봐   : ㅂ ㅗ %_ /vb + ㅇ ㅏ %_ /ef => ㅂ /vb + ㅘ/ef 
	 * ! 가   : ㄱ ㅏ %_ /vb + ㅇ ㅏ %_ /ef => ㄱ /vb + ㅏ/ef
	 * ! 됐다 : ㄷ ㅚ %_ /vb + ㅇ ㅓ %_ㅆ /ep + ㄷ ㅏ %_ /ef => ㄷ /vb + ㅙㅆ/ep ..
	 * !
	 * ! 오아서(x), 와서(o) ('오'의 경우는 반드시 줄여서 사용한다)
	 * ! 보아서(o), 봐서(o), 보아도(o), 보아서(o) ('보'의 경우는 줄이는 것이 옵션)
	 * !
	 * ! 건너도(o), 건너(o), 건너서(o), 가도(o), 건너어도(x), 가아도(x)
	 * !
	 * ! 났다 (나 + 았다), 나았다 (낫 + 았다)
	 * ! 나아도(o), 나도(x), 나(o) (낫+아), 나도(x) (낫 + 아도) (/irrs 줄임 안됨)
	 * ! 내저어(o), 내저(x) (내젓/irrs/vb + 어/ef) (/irrs의 경우 줄임 안됨)
	 * ! 되어도(o), 되어(o), 돼(o) (되+어), 돼도(o)
	 * ! 주어도(o), 줘도(o), 주어(o)
	 * ! 피어도(o), 피어(o), 펴(o)
	 * ! 고아도(o), 고아(o), 고아서(o), 과서 (국을 과서) 
	 * ! 굶주리어도(o), 굶주려도(o)
	 * !
	 * ! 겨눠도(o), 겨눠(o), 겨누어(o)
	 * ! 곁들여도(o), 곁들여(o), 곁들이어(o)
	 * ! 키워도(o), 키워(o), 키우어(x) 
	 * ! 치워도(o), 치워(o), 치우어(x)
	 * ! ('우' + '어' => 안된다. 용언의 마지막 음절이 'ㅇ'이 있을 때는 항상 줄임)
	 * ! ('오' + '아' => 안된다. 용언의 마지막 음절이 'ㅇ'이 있을 때는 항상 줄임)
	 * !
	 * ! 붓다 + 어 => 부어. (붜 는 안됨.)
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define MarkDiphOA		(IrrSet) VerbStringSet (@->) %[DIPH1 ... %]DIPH1 || [Onset - ㅇ] ㅗ (FILLC) _ ㅇ ㅏ ; ! ㅘ
	 * define MarkDiphUEO		(IrrSet - %/irrs) VerbStringSet (@->) %[DIPH2 ... %]DIPH2 || [Onset - ㅇ] ㅜ (FILLC) _ ㅇ ㅓ ; ! ㅝ
	 * define MarkDiphWAEO		(IrrSet) VerbStringSet (@->) %[DIPH3 ... %]DIPH3 || ㅚ (FILLC) _ ㅇ ㅓ              ; ! ㅙ
	 * 
	 * ! For compiling in HFST, use the follwing rewrite-rule in equivalent notation
	 * !define MarkDiphOA		[ $[(IrrSet) VerbStringSet @-> %[DIPH1 ... %]DIPH1 || [Onset - ㅇ] ㅗ (FILLC) _ ㅇ ㅏ ] ]* ; ! ㅘ
	 * !define MarkDiphUEO		[ $[(IrrSet - %/irrs) VerbStringSet @-> %[DIPH2 ... %]DIPH2 || [Onset - ㅇ] ㅜ (FILLC) _ ㅇ ㅓ ] ]* ; ! ㅝ
	 * !define MarkDiphWAEO		[ $[(IrrSet) VerbStringSet @-> %[DIPH3 ... %]DIPH3 || ㅚ (FILLC) _ ㅇ ㅓ ] ]*              ; ! ㅙ
	 * 
	 * define MarkDiphUA		(IrrSet) VerbStringSet  @->  %[DIPH4 ... %]DIPH4 || ㅜ (FILLC) _ ㅇ ㅏ	             ; ! ㅜ ㅏ
	 * define MarkDiphAA		(IrrSet - %/irrs) VerbStringSet @->
	 * 													 %[DIPH5 ... %]DIPH5 || ㅏ (FILLC) _ ㅇ ㅏ              ; ! ㅏ
	 * define MarkDiphEOEO		(IrrSet - %/irrs) VerbStringSet @->
	 * 													 %[DIPH6 ... %]DIPH6 || ㅓ (FILLC) _ ㅇ ㅓ              ; ! ㅓ
	 * define MarkDiphOA2		(IrrSet) VerbStringSet  @->  %[DIPH1 ... %]DIPH1 || ㅇ ㅗ (FILLC) _ ㅇ ㅏ           ; ! ㅘ
	 * define MarkDiphUEO2		(IrrSet) VerbStringSet  @->  %[DIPH2 ... %]DIPH2 || ㅇ ㅜ (FILLC) _ ㅇ ㅓ           ; ! ㅝ
	 * 
	 * define MarkDiph			MarkDiphOA .o. MarkDiphUEO .o. MarkDiphWAEO .o. MarkDiphUA .o. 
	 * 						MarkDiphAA .o. MarkDiphEOEO .o. MarkDiphOA2 .o. MarkDiphUEO2 ;
	 * 
	 * define RuleDiph			ㅗ (FILLC) %[DIPH1 -> 0 , %]DIPH1 ㅇ ㅏ -> ㅘ ,
	 * 					    ㅜ (FILLC) %[DIPH2 -> 0 , %]DIPH2 ㅇ ㅓ -> ㅝ ,
	 * 					    ㅚ (FILLC) %[DIPH3 -> 0 , %]DIPH3 ㅇ ㅓ -> ㅙ ,
	 * 						ㅜ (FILLC) %[DIPH4 -> 0 , %]DIPH4 ㅇ ㅏ -> ㅘ ,
	 * 					    ㅏ (FILLC) %[DIPH5 -> 0 , %]DIPH5 ㅇ ㅏ -> ㅏ , (x)
	 * 					    ㅓ (FILLC) %[DIPH6 -> 0 , %]DIPH6 ㅇ ㅓ -> ㅓ ; (x)
	 * 
	 * define ConjDiph			MarkDiph .o. RuleDiph ;
	 * 
	 * @param info
	 */
	public KeywordRef getConjDiph(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		char[] nextStart = n.getNextStart();
	
		char cb = Utils.EMPTY_JONGSEONG;
		if(Utils.isMatch(prevEnd, i, 1) //{'ㅣ'}
			&& Utils.isMatch(nextStart, eo, 1)){ //{'ㅓ'}
			
			cb = 'ㅕ';
		}else if(Utils.isMatch(prevEnd, o, 1) //{'ㅗ'}
			&& Utils.isMatch(nextStart, a, 1)){ //{'ㅏ'}
			
			cb = 'ㅘ';
		}else if(Utils.isMatch(prevEnd, u, 1) //{'ㅜ'}
			&& Utils.isMatch(nextStart, eo, 1)){ //{'ㅓ'}
		
			cb = 'ㅝ';
		}else if(Utils.isMatch(prevEnd, oe, 1) //{'ㅚ'}
			&& Utils.isMatch(nextStart, eo, 1)){ //{'ㅓ'}
		
			cb = 'ㅙ';
		}else if(Utils.isMatch(prevEnd, u, 1) //{'ㅜ'}
			&& Utils.isMatch(nextStart, a, 1)){ //{'ㅏ'}
		
			cb = 'ㅘ';
		}
		
		if(cb != Utils.EMPTY_JONGSEONG){
			char middle = Utils.compound(prevEnd[0], cb, nextStart[2]);
			
			String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord.substring(1);
			
			keyword = createKeywordRef(str, prev, next);
		}
		
		return keyword;
	}
	
	public boolean isConjDiph(PrevInfo info) {
		
		char[] prevEnd = info.getPrevEnd();
		
		if(Utils.isMatch(prevEnd, iouoe, 1) ){//{'ㅣ','ㅗ','ㅜ','ㅚ'}
			
			return true;
		}
		
		return false;
	}
	
	public boolean isConjDiph(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, eoayeo, Utils.JONGSEONG)){ // {'ㅇ'}, {'ㅓ','ㅏ','ㅕ'}
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 'ㅎ' 불규칙 활용
	 * !
	 * ! 형용사 어간 끝 'ㅎ'이 어미 'ㄴ', 'ㄹ', 'ㅁ' 'ㅂ' 앞에서 사라지고,
	 * ! 어미 '아/어' 앞에서 'ㅣ'로 바뀌어 합쳐지는 활용.
	 * !
	 * ! * 그럽니다, 까맙니다. 동그랍니다, 퍼럽니다. 하얍니다 등의 용례는
	 * !   1994년 12월 16일에 열린 국어 심의 회의 결정에 따라 삭제되었음.
	 * !   다만 구어적으로 사용되는 경우가 많아서 'ㅂ'의 경우도 처리함.
	 * !
	 * ! 까맣다 : 까매, 까만, 까마니, 까마면, 까맸다, 까맙니다, 까맣소.
	 * ! 하얗다 : 하얘, 하얀, 하야니, 하야면, 하앴다, 하얍니다. 하얗소.
	 * ! 말갛다 : 말개, 말간, 말가니, 말가면, 말갰다, 말갑니다. 말갛소.
	 * ! 멀겋다 : 멀게, 멀건, 멀거니, 멀거면, 멀겠다, 멀겁니다. 멀겋소.
	 * ! 
	 * ! 퍼레, 뻘게, 누레, 파라면 (파랗 + 으면 => 파라면), 파랬다, 퍼렜다
	 * ! 멀게 (멀겋 + 어 (?)), 퍼레, 퍼레지다, 허얘 (허옇)
	 * !
	 * ! 규칙) 놓다, 넣다, 낳다, 찧다, 쌓다, 좋다, 싫다, 많다, 괜찮다
	 * ! 그런데, '빨리 놔 (놓+ㅏ)'와 같이 ㅎ 불규칙으로 사용하는 경우가 많다.
	 * ! '따 논 당상' (놓+은) '놔'에 대해서만 특별히 처리하겠다.
	 * !
	 * ! 까마니 : ㄲ ㅏ %_ ㅁ ㅏ %_ㅎ /irrh/vj	+          ㄴ ㅣ %_ /ec
	 * !          ㄲ ㅏ %_ ㅁ ㅏ %_ㅎ /irrh/vj + ㅇ ㅡ %_ ㄴ ㅣ %_ /ec (으 삽입)
	 * !		   ㄲ ㅏ %_ ㅁ ㅏ      /irrh/vj + ㅇ ㅡ %_ ㄴ ㅣ %_ /ec (ㅎ 삭제)
	 * !          ㄲ ㅏ %_ ㅁ ㅏ      /irrh/vj +          ㄴ ㅣ %_ /ec (으 삭제)
	 * !
	 * ! 파랬다 : ㅍ ㅏ %_ ㄹ ㅏ %_ㅎ /irrh/vj + ㅇ ㅏ %_ㅅㅅ /ep
	 * !		   ㅍ ㅏ %_ ㄹ         /irrh/vj +    ㅐ %_ㅅㅅ /ep (ㅏ %_ㅎ ㅇ => ㅐ)
	 * ! 퍼레   : ㅍ ㅓ %_ ㄹ ㅓ %_ㅎ /irrh/vj + ㅇ ㅓ %_ㅅㅅ /ep (ㅓ %_ㅎ ㅇ => ㅔ)
	 * ! 하얘	 : ㅎ ㅏ %_ ㅇ ㅑ %_ㅎ /irrh/vj + ㅇ ㅓ %_	   /ec (ㅑ %_ㅎ ㅇ => ㅒ)
	 * ! 허예   : ㅎ ㅓ %_ ㅇ ㅕ %_ㅎ /irrh/vj + ㅇ ㅓ %_     /ec (ㅕ %_ㅎ ㅇ => 예)
	 * !
	 * ! *) ~놔   ==> 놓 + 아 형태로 특별히 만든다. 
	 * ! 하앴다 : 하얗다의 과거형.
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define ChangeHRule  ㅏ %_ㅎ %/irrh %/vj ㅇ ㅏ -> %/irrh %/vj ㅐ ,
	 *                     ㅑ %_ㅎ %/irrh %/vj ㅇ ㅓ -> %/irrh %/vj ㅒ ,
	 *                     ㅓ %_ㅎ %/irrh %/vj ㅇ ㅓ -> %/irrh %/vj ㅔ ,
	 *                     ㅕ %_ㅎ %/irrh %/vj ㅇ ㅓ -> %/irrh %/vj ㅖ ,
	 *                     ㅓ %_ㅎ %/irrh %/vi ㅇ ㅓ -> %/irrh %/vi ㅔ ;
	 * 
	 * define PhnOnsetNLM	[ ㄴ | ㄹ | ㅁ ] ;
	 * define PhnCodaNLM	[ %_ㄴ | %_ㄹ | %_ㅁ ] ;
	 * define ChangeH		%_ㅎ -> 0 || _ %/irrh [%/vj | %/vi] 
	 * 								[ ㅇ ㅡ [ PhnCodaNLM | FILLC PhnOnsetNLM ] | %_ㅂ ] ;
	 * define DeleteHEU1	ㅇ ㅡ FILLC -> 0 || %/irrh [%/vj | %/vi] _ PhnOnsetNLM ;
	 * define DeleteHEU2	ㅇ ㅡ       -> 0 || %/irrh [%/vj | %/vi] _ PhnCodaNLM  ;
	 * define DeleteHEU	DeleteHEU1 .o. DeleteHEU2 ;
	 * 
	 * define IrrConjH		ChangeHRule .o.  ChangeH .o. DeleteHEU1 .o. DeleteHEU2 ;
	 * 
	 * @param info
	 */
	public KeywordRef getIrrConjH(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		char[] nextStart = n.getNextStart();
		
		if(nextStart.length > 1){ //{'ㅇ'}

			char cb = Utils.EMPTY_JONGSEONG;
			
			if(Utils.isMatch(prevEnd, a, 1) //{'ㅏ'}
				&& Utils.isMatch(nextStart, a, 1)){ //{'ㅏ'}
				
				cb = 'ㅐ';
			}else if(Utils.isMatch(prevEnd, ya, 1) //{'ㅑ'}
				&& Utils.isMatch(nextStart, eo, 1)){ //{'ㅓ'}
				
				cb = 'ㅒ';
			}else if(Utils.isMatch(prevEnd, eo, 1) //{'ㅓ'}
				&& Utils.isMatch(nextStart, eo, 1)){ //{'ㅓ'}
				
				cb = 'ㅔ';
			}else if(Utils.isMatch(prevEnd, yeo, 1) //{'ㅕ'}
				&& Utils.isMatch(nextStart, eo, 1)){ //{'ㅓ'}
				
				cb = 'ㅖ';
			}
			
			if(cb != Utils.EMPTY_JONGSEONG){
				char middle = Utils.compound(prevEnd[0], cb, nextStart[2]);
				String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord.substring(1);
				
				keyword = createKeywordRef(str, prev, next);
			}
			
		}else {
			
			char middle = Utils.compound(prevEnd[0], prevEnd[1], nextStart[0]);
			
			String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord.substring(1);
			
			keyword = createKeywordRef(str, prev, next);
		}
		
		return keyword;
	}
	
	public boolean isIrrConjH(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		if(Utils.isIrrRule(prev, IrrRule.irrh)){

			return true;
			
		}
		
		return false;
	}
	
	public boolean isIrrConjH(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, aeo, Utils.JONGSEONG)){ //{'ㅇ'}, {'ㅏ','ㅓ'}
			
			return true;
			
		}else if(irrhr.run(nextStart, 0, nextStart.length)){
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! '거라' 불규칙/'너라' 불규칙
	 * !
	 * ! 가거라(가+어라), 보거라(보+어라), 있거라(있+어라), 자거라(자+거라), 
	 * ! 나거라(나+어라), 삼가거라(삼가+어라), 오너라 (오+어라)
	 * !
	 * ! 가라(가+라), 보라(보+라), 봐라(보+아라), 있어라(있+어라), 
	 * ! 자라(자+라), 나라(나+라), 오라(오+라  ), 와라(오+아라)
	 * !
	 * ! 있어라 | 있거라
	 * !
	 * ! 가거라 => ㄱ ㅏ %_ /vb + ㅇ ㅓ %_ ㄹ ㅏ %_ (고) /e*
	 * !           ㄱ ㅏ %_ /vb + ㄱ ㅓ %_ ㄹ ㅏ %_ (고) /e*
	 * !
	 * ! 있거라 => ㅇ ㅣ %_ㅅㅅ /vb + ㅇ ㅓ %_ ㄹ ㅏ %_ (고) /e*
	 * !           ㅇ ㅣ %_ㅅㅅ /vb + ㄱ ㅓ %_ ㄹ ㅏ %_ (고) /e*
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define IrrGeola1	ㅇ ->   ㄱ || [[ㄱ|ㅈ|ㄴ]ㅏ|ㅂ ㅗ] FILLC VerbSet _ ㅓ FILLC ㄹ ㅏ FILLC ;
	 * define IrrGeola2	ㅇ (->) ㄱ || ㅇ ㅣ %_ㅅㅅ VerbSet _ ㅓ FILLC ㄹ ㅏ FILLC ;
	 * define IrrNeola		ㅇ ->   ㄴ || ㅇ ㅗ FILLC VerbSet _ ㅓ FILLC ㄹ ㅏ FILLC ;
	 * define IrrEola		IrrGeola1 .o. IrrGeola2 .o. IrrNeola ;
	 * 
	 * @param info
	 */
	public KeywordRef getIrrEola(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] nextStart = n.getNextStart();
		
		char choseong = 'ㄱ';
		
		if(Utils.endWith(prev, "오")){
			
			choseong = 'ㄴ';
		}
		
		char middle = Utils.compound(choseong, nextStart[1], nextStart[2]);
		
		String str = prevWord + middle + nextWord.substring(1);
		
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isIrrEola(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		char[] prevEnd = info.getPrevEnd();
		
		if(irrGeolar.run(prevEnd, 0, prevEnd.length)
			&& Utils.isTag(prev, POSTag.v)){
			
			return true;
			
		}else if(Utils.endWith(prev, "오")
			&& Utils.isTag(prev, POSTag.v)){
			
			return true;
			
		}
		
		return false;
	}
	
	public boolean isIrrEola(NextInfo info) {
		
		Keyword next = info.getNext();
		
		if(Utils.startsWith(next, "어라")){
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! '러' 불규칙 (/irrL) : 이르러 (이르 + 어)
	 * !
	 * ! '르'로 끝나는 어간 뒤에서 '어'가 '러'로 바뀌는 활용
	 * !
	 * ! 이르러 => 이르/irrL/vb + 어/ec
	 * !        => ㅇ ㅣ %_ ㄹ ㅡ %_ /irrL/vb ㅇ ㅓ %_ /ec
	 * !        => ㅇ ㅣ %_ ㄹ ㅡ %_ /irrL/vb ㄹ ㅓ %_ /ec
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define IrrConjL		~$[ %/irrL VerbStringSet ㄹ [ㅓ|ㅏ] ] .o.
	 * 					[ ㅇ -> ㄹ || %/irrL VerbStringSet _ ㅓ ] ;
	 * 
	 * @param info
	 */
	public KeywordRef getIrrConjL(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] nextStart = n.getNextStart();
			
		char middle = Utils.compound('ㄹ', nextStart[1], nextStart[2]);
		
		String str = prevWord + middle + nextWord.substring(1);
		
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isIrrConjL(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		if(Utils.isIrrRule(prev, IrrRule.irrL)){
			
			return true;
		}
		
		return false;
	}
	
	public boolean isIrrConjL(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, eo, Utils.JONGSEONG)){ //{'ㅇ'}, {'ㅓ'}
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 'ㅂ' 불규칙 (/irrb) : 도우면 (돕 + 면)
	 * !
	 * ! 어간의 'ㅂ' 받침이 모음으로 시작하는 어미 앞에서 'ㅜ'로 바뀌는 활용.
	 * !
	 * ! 도우면 => 돕/irrb/vb + 으 면/ec                (으 삽입 후 ㅂ 불규칙 처리)
	 * !        => ㄷ ㅗ %_ ㅇ ㅜ /irrb/vb + ㅇ ㅡ %_ 면/ec (우측모음 'ㅡ' -> '우')
	 * !        => ㄷ ㅗ %_ ㅇ ㅜ /irrb/vb + 면/ec              ('ㅇ ㅡ %_'를 삭제) 
	 * !        => 도우면 (FillNullCoda에서 %_가 삽입되게 된다.)
	 * !
	 * ! 도와   => 돕/irrb/vb + 아/ec
	 * !        => ㄷ ㅗ %_ ㅇ ㅜ   /irrb/vb + ㅇ ㅏ %_ /ec
	 * !        => ㄷ ㅗ %_ ㅇ      /irrb/vb +    ㅘ /ec
	 * !
	 * ! 도운   : 돕 + ㄴ => 돕 + ㅇ ㅡ ㄴ => 도우 + ㅇ ㅡ ㄴ => 도우 + ㄴ => 도운
	 * ! 도우니 : 돕 + 니 => 돕 + ㅇ ㅡ %_ 니 => 도우 + 니 => 도우니
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define PhnOnsetCtxt	[ ㄴ | ㄹ | ㅁ | ㅅ ㅣ | ㅇ ㅗ ] ;
	 * define PhnCodaCtxt	[ %_ㄴ | %_ㄹ | %_ㅁ ] ;
	 * define ChangeB		%_ㅂ -> FILLC ㅇ ㅜ || _ %/irrb VerbStringSet ㅇ ;
	 * define DeleteBEU	ㅇ ㅡ (FILLC) @-> 0 || %/irrb VerbStringSet _ [ PhnOnsetCtxt | PhnCodaCtxt ] ;
	 * define IrrConjB		ChangeB .o. DeleteBEU ;
	 * 
	 * @param info
	 */
	public KeywordRef getIrrConjB(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		char[] nextStart = n.getNextStart();
		
			
		char middle = Utils.compound(prevEnd[0], prevEnd[1]);
		
		char[] tmp = nextStart;
		
		//DeleteBEU
		if(Utils.isMatch(nextStart, ng, eu, nrm)){ //{'ㅇ'}, {'ㅡ'}, {'ㄴ', 'ㄹ', 'ㅁ'}
			tmp = new char[]{nextStart[2]};
		}
		
		String str;
		
		//종성만 남은 경우
		if(tmp.length == 1){
			char middle2 = Utils.compound('ㅇ', 'ㅜ', tmp[0]);
			str = prevWord.substring(0, prevWord.length() - 1) + middle + middle2 + nextWord.substring(1);
		}else{
			char middle2 = '우';
			
			if(tmp[1] == 'ㅏ'){
				
				middle2 = Utils.compound('ㅇ', 'ㅘ', tmp[2]);
				
				str = prevWord.substring(0, prevWord.length() - 1) + middle + middle2 + nextWord.substring(1);
			}else{
				str = prevWord.substring(0, prevWord.length() - 1) + middle + middle2 + nextWord;
			}
		}
		
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isIrrConjB(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		if(Utils.isIrrRule(prev, IrrRule.irrb)){
			
			return true;
		}
		
		return false;
	}
	
	public boolean isIrrConjB(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, 0)){ //{'ㅇ'}
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 'ㄷ' 불규칙 현상 (/irrd) : 깨달아 (깨닫 + 아)
	 * !
	 * ! 어간의 'ㄷ' 받침이 모음으로 시작하는 어미 앞에서 'ㄹ' 받침으로 바뀌는 활용
	 * ! 동사에만 있고 형용사에는 없음.
	 * !
	 * ! 깨닫 + 아 => 깨달아, 붇 + 어 => 불어, 깨닫 + 니 => 깨달 으 니
	 * !
	 * ! 규칙활용이 있음 : 믿 + 어 => 믿어
	 * !
	 * ! 깨닫/irrd/vb + 았/ep + 다/ef
	 * !
	 * !     => ㄷ ㅏ %_ㄷ /irrd /vb + ㅇ ㅏ %_ㅆ /ep + ㄷ ㅏ %_ /ef
	 * !     => ㄷ ㅏ %_ㄹ /irrd /vb + ㅇ ㅏ %_ㅆ /ep + ㄷ ㅏ %_ /ef
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define IrrConjD		%_ㄷ -> %_ㄹ || _ %/irrd %/vb ㅇ ;
	 * 
	 * @param info
	 */
	public KeywordRef getIrrConjD(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		
		char middle = Utils.compound(prevEnd[0], prevEnd[1], 'ㄹ');
		String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord;

		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isIrrConjD(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		if(Utils.isIrrRule(prev, IrrRule.irrd)
			&& Utils.isTag(prev, POSTag.vb)){
			
			return true;
		}
		
		return false;
	}
	
	public boolean isIrrConjD(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, 0)){ //{'ㅇ'}
			
			return true;
		}
		
		return false;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 'ㅅ' 불규칙 현상 (/irrs) : 그었다 (긋 + 었다)
	 * !
	 * ! 'ㅅ' 불규칙은 어간의 ㅅ 받침이 모음으로 시작하는 어미 앞에서 탈락하는 활용
	 * !
	 * ! 긋 + 어 => 그어, 낫 + 어 => 나어
	 * !
	 * ! 규칙활용이 있음 : 벗 + 어 => 벗어
	 * !
	 * ! 긋/irrs/vb + 었/ep + 다/ef 
	 * !
	 * !     => ㄱ ㅡ ㅅ /irrs /vb + ㅇ ㅓ %_ㅆ /ep + ㄷ ㅏ %_ /ef
	 * !     => ㄱ ㅡ    /irrs /vb + ㅇ ㅓ %_ㅆ /ep + ㄷ ㅏ %_ /ef
	 * !
	 * ! 긋/irrs/vb + 니/ec => 긋/irrs/vb + 으 니/ec => 그으니
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define DropS		%_ㅅ -> 0 || _ %/irrs VerbStringSet ㅇ ;
	 * 
	 * @param info
	 */
	public KeywordRef getDropS(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		
		char middle = Utils.compound(prevEnd[0], prevEnd[1]);
		String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord;

		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isDropS(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		if(Utils.isIrrRule(prev, IrrRule.irrs)){ 
			
			return true;
		}
		
		return false;
	}
	
	public boolean isDropS(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, 0)){ //{'ㅇ'}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! 'ㄹ' 탈락 현상 : 잘 아네 (알 + 네)
	 * !
	 * ! 오른쪽에는 '종성 ㄴ' '초성 ㄴ' '종성 ㅂ' '시' '오' '종성 ㄹ' '세' 가 온다.
	 * !
	 * ! 'ㄹ' 탈락은 용언의 어간이 'ㄹ'로 끝나고 그 뒤에 어미 '종성 ㄴ' '초성 ㄴ' 
	 * ! 'ㅂ니다' '시' '세요' '오' 등이 올 때 'ㄹ'이 탈락하는 현상
	 * !
	 * ! 알면, 알고, 안, 아네, 압니다, 아시지요, 아신지, 아세요, 아오, 아니까
	 * ! 길면, 길고, 긴, 기네, 깁니다, 기시지요, 기신지, 기세요, 기오, 기니까
	 * !
	 * ! 'ㄹ' 탈락 규칙 용언 : 살다, 울다, 놀다, 불다, 갈다, 멀다, 달다, 둥글다, 어질다
	 * !
	 * ! 알/vb + ㅂ니다/ef => ㅇ ㅏ %_ㄹ/vb + %_ㅂ ㄴ ㅣ %_ ㄷ ㅏ %_ /ef
	 * ! 알/vb + 니까/ef   => ㅇ ㅏ %_ㄹ/vb + ㄴ ㅣ %_ ㄲ ㅏ %_ /ef
	 * !                   => ㅇ ㅏ     /vb + ㄴ ㅣ %_ ㄲ ㅏ %_ /ef
	 * !
	 * ! *나중에 "모음 용언품사 자음" 순으로 심벌이 진행될 때 사이에 %_를 넣는다. 
	 * !
	 * ! * '오' 불규칙 활용
	 * !
	 * !   '달/vb' + '아라'일 때 '아라'가 '오'로 변형된 후, 'ㄹ' 탈락 현상 뒤에
	 * !   '다오'로 바뀐다. (말하는 이가 듣는 이에게 어떤 것을 주도록 요구하는 동사)
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define IrrConjO		ㅏ FILLC ㄹ ㅏ FILLC -> ㅗ || ㄷ ㅏ %_ㄹ %/vb ㅇ _ %/ef ;
	 * 
	 * define DropL		%_ㄹ -> 0 || _ VerbStringSet 
	 * 								[%_ㄴ | %_ㅂ | %_ㄹ | ㄴ | [ㅅ ㅣ] | [ㅇ ㅗ] | [ㅅ ㅔ]] ; 
	 * 
	 * @param info
	 */
	public KeywordRef getDropL(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		char[] nextStart = n.getNextStart();

		char middle;
		String str;
		
		//종성만 있는 경우
		if(nextStart.length == 1){
			middle = Utils.compound(prevEnd[0], prevEnd[1], nextStart[0]);
			str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord.substring(1);
		}else{
			middle = Utils.compound(prevEnd[0], prevEnd[1]);
			str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord;
		}
	
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isDropL(PrevInfo info) {
		
		char[] prevEnd = info.getPrevEnd();
		
		if(Utils.isMatch(prevEnd, r, 2) ){ //{'ㄹ'}
			
			return true;
		}
		
		return false;
	}
	
	public boolean isDropL(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(dropLr.run(nextStart, 0, nextStart.length)){
			
			return true;
		}
		
		return false;
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
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
	 */
	public KeywordRef getInsertEU(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] nextStart = n.getNextStart();
		
		if(nextStart.length > 1){
			
			//매개모음 '으' 추가여부 결정 필요
			String str = prevWord + "으" + nextWord;
//				logger.info("word : {}, prev : {} ({}), next :{} ({})", str, prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			
			keyword = createKeywordRef(str, prev, next);
		}else if(nextStart.length == 1){
			
			char middle = Utils.compound('ㅇ', 'ㅡ', nextStart[0]);
			
			String str = prevWord + middle + nextWord.substring(1);
			
//				logger.info("word : {}, prev : {} ({}), next :{} ({})", str, prev.getWord(), prev.getTag(), next.getWord(), next.getTag());
			
			keyword = createKeywordRef(str, prev, next);
		}
		
		return keyword;
	}
	
	public boolean isInsertEU(PrevInfo info) {
		
		char[] prevEnd = info.getPrevEnd();
		
		if(Utils.isMatch(prevEnd, jongseong, 2)){
			
			return true;
		}
		
		return false;
	}
	
	public boolean isInsertEU(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(eu1r.run(nextStart, 0, nextStart.length)){
			
			return true;
		}else if(eu2r.run(nextStart, 0, nextStart.length)){
			
			return true;
		}
		
		return false;
	}


	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !                                                                                 !
	 * ! '으' 탈락 현상 : 모아 : 모으 + 아                                               !
	 * !                                                                                 !
	 * ! 아프 /vb + 았 /ep + 고 /ec => 아 ㅍ ㅡ %_ %[VBEU /vb %]VBEU ㅇ ㅏ ㅆ /ep        !
	 * !                            => 아 ㅍ /vb + ㅏ ㅆ /ep + 고 /ec                    !
	 * !                                                                                 !
	 * ! '으'로 끝나는 모든 용언 + '아'/'어'로 시작하는 어미가 올 때 '으'가 탈락된다.    !
	 * !                                                                                 !
	 * ! 쓰지만, 쓰고, 썼습니다 (쓰 + 었습니다), 써서 (쓰 + 어도), 써도 (쓰 + 어도)      !
	 * ! 아프지만, 아프고, 아팠습니다, 아파서, 아파도                                    !
	 * !                                                                                 !
	 * ! '으' 탈락규칙 용언 : 쓰다 (글이), 따르다, 뜨다, 끄다, 담그다, 기쁘다, 바쁘다    !
	 * !                      슬프다, 쓰다 (맛이)                                        !
	 * !                                                                                 !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define MarkVerbEU	VerbStringSet -> %[VBEU ... %]VBEU || Onset ㅡ FILLC _ ㅇ [ㅏ | ㅓ] ;
	 * define DropEU       MarkVerbEU .o. [ ㅡ FILLC %[VBEU -> 0 ] .o. [ %]VBEU ㅇ -> 0 ] ;
	 * 
	 * @param info
	 */
	public KeywordRef getDropEU(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd = p.getPrevEnd();
		char[] nextStart = n.getNextStart();
		
		char middle = Utils.compound(prevEnd[0], nextStart[1], nextStart[2]);
		
		String str = prevWord.substring(0, prevWord.length() - 1) + middle + nextWord.substring(1);
		
//		logger.info("word : {}, prev : {}, next :{}", str, prev, next);
		
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}

	public boolean isDropEU(PrevInfo info) {
		
		char[] prevEnd = info.getPrevEnd();
		
		if(Utils.isMatch(prevEnd, Utils.CHOSEONG, eu) ){ //{'ㅡ'}
			
			return true;
		}
		
		return false;
	}

	public boolean isDropEU(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, aeo, Utils.JONGSEONG)){ //{'ㅇ'}, {'ㅏ','ㅓ'}
			
			return true;
		}
		
		return false;
	}

	
	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! '하다' + '어'   => '하여'    '하다' + '어'   => '해' 
	 * ! '하다' + '어서' => '하여서'  '하다' + '어서' => '해서'
	 * ! '하다' + '었다' => '하였다'  '하다' + '었다' => '했다'
	 * !
	 * ! 하여  => * ㅎ ㅏ %_ /vb + ㅇ ㅓ * /e*
	 * !   (1) => * ㅎ ㅏ %_ /vb + ㅇ ㅕ * /e*
	 * !   (2) => * ㅎ       /vb +    ㅐ * /e*
	 * !       => (1) | (2)
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define IrrConjYEO1	ㅓ -> ㅕ || ㅎ ㅏ FILLC [%/vb|%/vi|%/vj|%/vx|%/xj|%/xv] ㅇ _ ;
	 * define IrrConjYEO2	ㅏ FILLC %/vb ㅇ ㅕ (->) %/vb ㅐ || ㅎ _  .o.
	 * 					ㅏ FILLC %/vi ㅇ ㅕ (->) %/vi ㅐ || ㅎ _  .o.
	 * 					ㅏ FILLC %/vj ㅇ ㅕ (->) %/vj ㅐ || ㅎ _  .o.
	 * 					ㅏ FILLC %/vx ㅇ ㅕ (->) %/vx ㅐ || ㅎ _  .o.
	 * 					ㅏ FILLC %/xj ㅇ ㅕ (->) %/xj ㅐ || ㅎ _  .o.
	 * 					ㅏ FILLC %/xv ㅇ ㅕ (->) %/xv ㅐ || ㅎ _  ;
	 * 
	 * define IrrConjYEO	IrrConjYEO1 .o. IrrConjYEO2 ;
	 * 
	 * @param info
	 * @return
	 */
	public KeywordRef getIrrConjYEO(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] nextStart = n.getNextStart();
		
		char middle = Utils.compound(nextStart[0], 'ㅕ', nextStart[2]);

		String str = prevWord + middle + nextWord.substring(1);

//		logger.info("word : {}, prev : {} , next :{} ", str, prev, next);
		
		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isIrrConjYEO(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		if (Utils.endWith(prev, "하")) { //{'ㅇ'}

			return true;
		}
		
		return false;
	}
	
	public boolean isIrrConjYEO(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if (Utils.isMatch(nextStart, ng, eo, Utils.JONGSEONG)) { //{'ㅇ'}, {'ㅓ'}

			return true;
		}
		
		return false;
	}

	
	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !
	 * ! '르' 불규칙 (/irrl) : 굴러 (구르 + 어) 
	 * !
	 * ! 어간의 끝소리 '르'가 어미 '아/어' 앞에서 'ㄹㄹ'로 바뀌는 활용
	 * !
	 * ! 굴러   => 구르/irrl/vb + 어/ec
	 * !        => ㄱ ㅜ %_   ㄹ ㅡ %_ /irrl/vb ㅇ ㅓ %_ /ec
	 * !        => ㄱ ㅜ %_ㄹ ㄹ       /irrl/vb    ㅓ %_ /ec
	 * !
	 * ! 몰랐다 => 모르/irrl/vb + 았/ep + 다/ef
	 * !        => ㅁ ㅗ %_   ㄹ ㅡ %_ /irrl/vb ㅇ ㅏ %_ㅅㅅ/ep
	 * !        => ㅁ ㅗ %_ㄹ ㄹ       /irrl/vb    ㅏ %_ㅅㅅ/ep
	 * !
	 * ! '몰랐다'는 모음 조화 현상에서 '몰'의 '오'에 의해 양성 '아'가 붙었음.
	 * !
	 * ! '이르다'는 '르' , '러' 변칙이 있어서, '이르러'일 때는 '러'변칙에만 해당
	 * !
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * define MarkIrrl		%/irrl VerbStringSet -> %[IRRl ... %]IRRl || FILLC ㄹ ㅡ FILLC _ ㅇ [ㅏ|ㅓ] ;
	 * define IrrConjl		~$[ ㅇ ㅣ FILLC ㄹ ㅡ FILLC %/irrl VerbStringSet ㄹ [ㅓ|ㅏ] ] .o.
	 * 					MarkIrrl .o. [ FILLC ㄹ ㅡ FILLC %[IRRl -> %_ㄹ ㄹ ] .o. [ %]IRRl ㅇ -> 0 ] ;
	 * 
	 * @param info
	 * @return
	 */
	public KeywordRef getIrrConjl(PrevInfo p, NextInfo n) {
		KeywordRef keyword = null;
		
		Keyword prev = p.getPrev();
		Keyword next = n.getNext();
		
		String prevWord = p.getPrevWord();
		String nextWord = n.getNextWord();

		char[] prevEnd2 = Utils.getCharAtDecompose(prev, -2);
		char[] nextStart = n.getNextStart();

//		logger.info("word : {}, prev : {} , next :{} ", "", prev, next);
		
		char middle1 = Utils.compound(prevEnd2[0], prevEnd2[1], 'ㄹ');
		char middle2 = Utils.compound('ㄹ', nextStart[1], nextStart[2]);
		
		String str = prevWord.substring(0, prevWord.length() - 2) + middle1 + middle2 + nextWord.substring(1);

		keyword = createKeywordRef(str, prev, next);
		
		return keyword;
	}
	
	public boolean isIrrConjl(PrevInfo info) {
		
		Keyword prev = info.getPrev();
		
		if(Utils.isIrrRule(prev, IrrRule.irrl)
			&& Utils.endWith(prev, "르")){ //{'ㅇ'}, {'ㅏ','ㅓ'}
			
			return true;
		}
		
		return false;
	}
	
	public boolean isIrrConjl(NextInfo info) {
		
		char[] nextStart = info.getNextStart();
		
		if(Utils.isMatch(nextStart, ng, aeo, Utils.JONGSEONG)){ //{'ㅇ'}, {'ㅏ','ㅓ'}
			return true;
		}
		
		return false;
	}
	
}
