package daon.analysis.ko;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.config.Config.CharType;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.util.Utils;

public class DaonAnalyzer {

	private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

	private Dictionary dictionary;
	
	public DaonAnalyzer(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public ResultTerms analyze(String text) throws IOException{
		//원본 문자
		char[] texts = text.toCharArray();
		
		//총 길이
		int textLength = text.length();

		//기분석 사전 매칭 정보 가져오기
		Map<Integer, List<Term>> lookupResults = dictionary.lookup(texts, 0, textLength);
		
//		logger.info("text : {}", text);
		
//		우리말은 제2 유형인 SOV에 속한다. 따라서 국어의 기본 어순은 ‘주어+목적어+서술어’이다.
		// 1순위) NN 맨 앞 + NN + (J) + V + (E)
		// 2순위) M 맨 앞 + V + (E)
		// 3순위) V 맨 앞
		
		//결과
		ResultTerms results = new ResultTerms(lookupResults);
		
		//원본 텍스트 길이 만큼 반복
		for(int idx=0; idx<textLength;){
			
			//idx에 해당하는 기분석 사전 결과 가져오기
			List<Term> currentTerms = results.get(idx);
			
//			System.out.println(idx + " - " + texts[idx] + " : " + currentTerms);

			/**
			 * 품사 결합 조건 체크
			 */
			//이전 추출 term 가져오기 
			Term prevTerm = results.getPrevTerm();
			
			//이전 기분석 사전 추출 term과 현재 기분석 사전 추추 term이 존재
			if(prevTerm != null && currentTerms != null){
				
				//이전 추출 term 이 체언(명사)인 경우 뒤에 조사/어미 여부를 체크
				if(isNoun(prevTerm)){
					
					//현재 기분석 사전 추출 term 들중 어느것을 가져와야될지..?
					Term t = lastTerm(currentTerms);
					if(isJosa(t)){
						prevTerm.setTag(POSTag.n);
						
						t.setTag(POSTag.p);
						
						//뽑힌 근거 추가.
						results.add(t);
						
						idx += t.getLength();
						
						continue;
					}
					
					if(isSuffix(t)){
						prevTerm.setTag(POSTag.n);
						
						t.setTag(POSTag.x);
						
						//뽑힌 근거 추가.
						results.add(t);
						
						idx += t.getLength();
						
						continue;
					}
				}
				
				//용언(동사) 인 경우 어미 여부 체크 ?
				if(isVerb(prevTerm)){
					
					Term t = firstTerm(currentTerms);
					if(isEomi(t)){

						t.setTag(POSTag.e);
						
						results.add(t);

						idx += t.getLength();
						
						continue;
					}
				}
				
				//접미사 인 경우 
				if(isSuffix(prevTerm)){
					Term t = firstTerm(currentTerms);
					if(isVerb(t)){

						//공통 함수 추출 -> 현재 텀에 품사 태깅 후 result 설정 idx 증가 
						t.setTag(POSTag.v); // <- 확정 된 품사에 일치하는 기분석 사전의 attr 품사 가져오기 
						
						results.add(t);

						idx += t.getLength();
						
						continue;
					}
					
				}
			}
			
			//현재 기분석 term이 존재하는 경우
			if(currentTerms != null){
				//좌 -> 우 최장일치법 or 최적 수치 사용?
				//마지막이 제일 긴 term 
				//선택되는 근거..? 
				Term lastTerm = lastTerm(currentTerms);
				int length = lastTerm.getLength();
				
				results.add(lastTerm);
				idx += length;
			}
			//미분석 term 처리
			else{
				Term unkownTerm = makeUnkownTerm(idx, texts, textLength, lookupResults);

				int length = unkownTerm.getLength();

				results.add(unkownTerm);
				idx += length;
			}
			
		}
		
		
		return results;
	}
	
	private Term firstTerm(List<Term> terms) {
		if(terms == null){
			return null;
		}
		
		Term t = terms.get(0);
		return t;
	}
	
	private Term lastTerm(List<Term> terms) {
		if(terms == null){
			return null;
		}
		
		int lastIdx = terms.size() -1;
		Term t = terms.get(lastIdx);
		
		return t;
	}
	
	
	/**
	 * 분리 예정 소스 
	 */
	public static final CharType[] charTypeTable;
	
	static {
		CharType[] tab = new CharType[256];
		for (int i = 0; i < 256; i++) {
			CharType code = CharType.ETC; 
			
			if (Character.isLowerCase(i)) {
				code = CharType.LOWER;
			} else if (Character.isUpperCase(i)) {
				code = CharType.UPPER;
			} else if (Character.isDigit(i)) {
				code = CharType.DIGIT;
			} else if (Character.isWhitespace(i)) { // 32 (spacebar), 9 (tab), 10 (new line), 13 (return)
				code = CharType.SPACE;
			}
			
			tab[i] = code;
		}
		
		charTypeTable = tab;
	}

	public CharType charType(int ch) {

		if (ch < charTypeTable.length) {
			return charTypeTable[ch];
		}

		// 한글 타입 체크
		if (ch >= Utils.KOR_START && ch <= Utils.KOR_END) {
			return CharType.KOREAN;
		}

		return getType(ch);
	}
	
	private CharType getType(int ch) {
		switch (Character.getType(ch)) {
		case Character.UPPERCASE_LETTER:
			return CharType.UPPER;
		case Character.LOWERCASE_LETTER:
			return CharType.LOWER;

		case Character.TITLECASE_LETTER:
		case Character.MODIFIER_LETTER:
		case Character.OTHER_LETTER:
		case Character.NON_SPACING_MARK:
		case Character.ENCLOSING_MARK: // depends what it encloses?
		case Character.COMBINING_SPACING_MARK:
			return CharType.ALPHA;

		case Character.DECIMAL_DIGIT_NUMBER:
		case Character.LETTER_NUMBER:
		case Character.OTHER_NUMBER:
			return CharType.DIGIT;

		// case Character.SPACE_SEPARATOR:
		// case Character.LINE_SEPARATOR:
		// case Character.PARAGRAPH_SEPARATOR:
		// case Character.CONTROL:
		// case Character.FORMAT:
		// case Character.PRIVATE_USE:

		case Character.SURROGATE: // prevent splitting
			return CharType.CHAR;

		// case Character.DASH_PUNCTUATION:
		// case Character.START_PUNCTUATION:
		// case Character.END_PUNCTUATION:
		// case Character.CONNECTOR_PUNCTUATION:
		// case Character.OTHER_PUNCTUATION:
		// case Character.MATH_SYMBOL:
		// case Character.CURRENCY_SYMBOL:
		// case Character.MODIFIER_SYMBOL:
		// case Character.OTHER_SYMBOL:
		// case Character.INITIAL_QUOTE_PUNCTUATION:
		// case Character.FINAL_QUOTE_PUNCTUATION:

		default:
			return CharType.ETC;
		}
	}
	
	/**
	 * 미분석 어절 구성
	 * 
	 * TODO 영문, 숫자, 한글 타입 구분
	 * TODO 공백 문자 처리
	 * 
	 * @param idx
	 * @param texts
	 * @param textLength
	 * @param lookupResults
	 * @return
	 */
	private Term makeUnkownTerm(int idx, char[] texts, int textLength, Map<Integer, List<Term>> lookupResults) {
		//기분석 결과에 없는 경우 다음 음절 체크
		int startIdx = idx;
		int endIdx = startIdx;
		
		char c = texts[endIdx];
		
		CharType lastType = charType(c);
		
		for(; endIdx < textLength; endIdx++){
//		while(endIdx < textLength){
			
			//타입 체크용, 현재 문자의 타입
			c = texts[endIdx];
			CharType type = charType(c);
			
			List<Term> terms = lookupResults.get(endIdx);
			
			if(terms != null){
				break;
			}
			
			if(isBreak(lastType, type)){
				break;
			}
			
			lastType = type;
		}

		int length = (endIdx - startIdx);
		
		String unkownWord = new String(texts, startIdx, length);
		
		//미분석 keyword
		Keyword word = new Keyword(unkownWord, "un");
		Term unknowTerm = new Term(word, startIdx, length);
		unknowTerm.setCharType(lastType);
		
		return unknowTerm;
	}

	public boolean isBreak(CharType lastType, CharType type) {
		if ((type.getBit() & lastType.getBit()) != 0) {
			return false;
		}

		//조합해야될 조건 지정 가능
		/*
		if (isAlpha(lastType) && isAlpha(type)) {
			// ALPHA->ALPHA: always ignore if case isn't considered.
			return false;
		} else if (isUpper(lastType) && isAlpha(type)) {
			// UPPER->letter: Don't split
			return false;
		}
//		else if (!splitOnNumerics && ((isAlpha(lastType) && isDigit(type)) || (isDigit(lastType) && isAlpha(type)))) {
//			// ALPHA->NUMERIC, NUMERIC->ALPHA :Don't split
//			return false;
//		}
		 */

		return true;
	}
	
	/**
	   * Checks if the given word type includes {@link #KOREAN}
	   *
	   * @param type Word type to check
	   * @return {@code true} if the type contains KOREAN, {@code false} otherwise
	   */
	  public boolean isKorean(CharType type) {
	    return (type.getBit() & CharType.KOREAN.getBit()) != 0;
	  }

	  /**
	   * Checks if the given word type includes {@link #ALPHA}
	   *
	   * @param type Word type to check
	   * @return {@code true} if the type contains ALPHA, {@code false} otherwise
	   */
	  public boolean isAlpha(CharType type) {
	    return (type.getBit() & CharType.ALPHA.getBit()) != 0;
	  }

	  /**
	   * Checks if the given word type includes {@link #DIGIT}
	   *
	   * @param type Word type to check
	   * @return {@code true} if the type contains DIGIT, {@code false} otherwise
	   */
	  public boolean isDigit(CharType type) {
	    return (type.getBit() & CharType.DIGIT.getBit()) != 0;
	  }
	

	private boolean isNoun(Term t){
		
		return contains(t, POSTag.n);
	}

	private boolean isJosa(Term t){
		
		return contains(t, POSTag.p);
	}

	private boolean isSuffix(Term t){
		
		return contains(t, POSTag.x);
	}



	private boolean isVerb(Term t){
		
		return contains(t, POSTag.v);
	}

	private boolean isEomi(Term t){
		
		return contains(t, POSTag.e);
	}
	
	private boolean contains(Term t, POSTag tag){
		boolean is = false;
		
		//확정 된 품사 우선 체크
		if(t.getTag() != null){
			long result = t.getTag().getBit() & tag.getBit();
			
			if(result > 0){
				return true;
			}
		}else if(t != null && t.getKeyword() != null){
			
			long tagBits = t.getKeyword().getTagBit();
			// 사전의 tag 정보와 포함여부 tag 의 교집합 구함.
			long result = tagBits & tag.getBit();
			
			if(result > 0){
				return true;
			}
		}
		
		return is;
	}
}
