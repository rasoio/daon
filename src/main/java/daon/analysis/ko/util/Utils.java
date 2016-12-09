package daon.analysis.ko.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.IrrRule;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.model.Keyword;

public class Utils {

	/**
	 * http://jrgraphix.net/r/Unicode/AC00-D7AF
	 */
	//한글 시작 문자
	public final static int KOR_START = 0xAC00;
	//한글 종료 문자
	public final static int KOR_END = 0xD7A3;
	
	/**
	 * 맨앞부터는 0,1,2
	 * 맨뒤부터는 -1,-2,-3
	 * 
	 * @param keyword
	 * @param idx
	 * @return
	 */
	public static char[] getCharAtDecompose(Keyword keyword, int idx){
		
		if(keyword == null) return new char[]{};
		
		String word = keyword.getWord();
		
		if(word == null) return new char[]{};
		
		int len = word.length();
		
		//마지막 문자 가져오기
		if(idx <= -1){
			idx = len + idx;
			
			if(idx < 0){
				return new char[]{};
			}
		}
		
		if(idx >= len){
			return new char[]{};
		}
		
		char c = word.charAt(idx);
		
		char[] lastchar = decompose(c);
		
		return lastchar;
	}
	
	public static char[] getFirstChar(Keyword keyword){

		return getCharAtDecompose(keyword, 0);
	}
	
	public static char[] getLastChar(Keyword keyword){
		
		return getCharAtDecompose(keyword, -1);
	}
	
	/**
	 * 
	 * @param c
	 * @param array
	 * @param findIdx 찾을 idx. 0 : 초성, 1 : 중성, 2 : 종성
	 * @return
	 */
	public static boolean isMatch(char[] c, char[] array, int findIdx){
		boolean isMatched = false;
		int chk = findIdx < 1 ? 1 : findIdx;
		int len = c.length;
		
		if(len == 0){
			return isMatched;
		}
		
		// 분리 내용이 있는 경우 있을 경우
		if(len > chk){
			int idx = ArrayUtils.indexOf(array, c[findIdx]);
			
			if(idx > -1){
				isMatched = true;
			}
		}
		
		return isMatched;		
	}
	
	public static boolean isMatch(char[] c, char[] choseong, char[] jungseong, char[] jongseong){
		boolean isMatched = false;
		
		if(isMatch(c, choseong, 0) 
				&& isMatch(c, jungseong, 1)
				&& isMatch(c, jongseong, 2)){
			isMatched = true;
		}
		
		return isMatched;
	}
	
	public static boolean isMatch(char[] c, char[] choseong, char[] jungseong){
		return isMatch(c, choseong, jungseong, NO_JONGSEONG);
	}
	
	public static boolean startsWith(Keyword keyword, char[] choseong, char[] jungseong, char[] jongseong){
		
		char[] c = getFirstChar(keyword);
		
		return isMatch(c, choseong, jungseong, jongseong);
	}
	
	public static boolean startsWith(Keyword keyword, char[] choseong, char[] jungseong){
		
		char[] c = getFirstChar(keyword);
		
		return isMatch(c, choseong, jungseong);
	}
	
	public static boolean startsWith(Keyword keyword, String[] prefixes){
		boolean isMatched = false;
		if(keyword == null) return isMatched;
		
		String word = keyword.getWord();
		
		if(word == null) return isMatched;
		
		return StringUtils.startsWithAny(word, prefixes);	
	}
	
	public static boolean startsWith(Keyword keyword, String prefix){
		return startsWith(keyword, new String[]{prefix});	
	}
	
	public static boolean startsWithChoseong(Keyword keyword, char[] choseong){
		
		char[] c = getFirstChar(keyword);
		
		return isMatch(c, choseong, 0);	
	}
	
	public static boolean startsWithChoseong(Keyword keyword, char choseong){
		return startsWithChoseong(keyword, new char[]{choseong});	
	}
	
	public static boolean startsWithChoseong(Keyword keyword){
		return startsWithChoseong(keyword, CHOSEONG);	
	}
	
	public static boolean startsWithJongseong(Keyword keyword, char[] jongseong){
		boolean isMatched = false;
		
		char[] c = getFirstChar(keyword);
		
		//종성만 있는 경우
		if(c.length == 1){
			int idx = ArrayUtils.indexOf(jongseong, c[0]);
			
			if(idx > -1){
				isMatched = true;
			}
		}
		
		return isMatched;	
	}
	
	public static boolean startsWithJongseong(Keyword keyword, char jongseong){
		return startsWithJongseong(keyword, new char[]{jongseong});	
	}
	
	public static boolean startsWithJongseong(Keyword keyword){
		return startsWithJongseong(keyword, JONGSEONG);	
	}
	
	
	public static boolean endWith(Keyword keyword, String[] suffixes){
		boolean isMatched = false;
		if(keyword == null) return isMatched;
		
		String word = keyword.getWord();
		
		if(word == null) return isMatched;
		
		return StringUtils.endsWithAny(word, suffixes);	
	}
	
	public static boolean endWith(Keyword keyword, String suffix){
		
		return endWith(keyword, new String[]{suffix});	
	}
	
	public static boolean endWith(Keyword keyword, char[] choseong, char[] jungseong, char[] jongseong){
		
		char[] c = getLastChar(keyword);
		
		return isMatch(c, choseong, jungseong, jongseong);
	}
	
	public static boolean endWith(Keyword keyword, char[] choseong, char[] jungseong){
		
		char[] c = getLastChar(keyword);
		
		return isMatch(c, choseong, jungseong);
	}
	
	public static boolean endWithChoseong(Keyword keyword, char[] choseong){
		char[] c = getLastChar(keyword);
		
		return isMatch(c, choseong, 0);		
	}
	
	public static boolean endWithChoseong(Keyword keyword, char choseong){
		return endWithChoseong(keyword, new char[]{choseong});
	}
	
	public static boolean endWithJungseong(Keyword keyword, char[] jungseong){
		char[] c = getLastChar(keyword);

		return isMatch(c, jungseong, 1);			
	}
	
	public static boolean endWithJungseong(Keyword keyword, char jungseong){
		return endWithJungseong(keyword, new char[]{jungseong});
	}
	
	public static boolean endWithJongseong(Keyword keyword, char[] jongseong){
		char[] c = getLastChar(keyword);

		return isMatch(c, jongseong, 2);	
	}
	
	public static boolean endWithJongseong(Keyword keyword, char jongseong){
		return endWithJongseong(keyword, new char[]{jongseong});
	}
	
	public static boolean endWithNoJongseong(Keyword keyword){
		
		char[] lc = getLastChar(keyword);

		return isMatch(lc, NO_JONGSEONG, 2);
	}
	
	public static boolean isLength(Keyword keyword, int len){
		boolean is = false;
		
		if(keyword == null) return is;
		
		String word = keyword.getWord();
		
		if(word == null ) return is;
		
		if(word.length() == len){
			is = true;
		}

		return is;
	}
	
	public static boolean isTag(Keyword keyword, POSTag[] tags){
		if (keyword == null || ArrayUtils.isEmpty(tags)) {
            return false;
        }
		
        for (final POSTag tag : tags) {
            if (isTag(keyword, tag)) {
                return true;
            }
        }
        return false;
	}
	
	public static boolean isTag(Keyword keyword, POSTag tag){
		boolean is = false;
		
		if(keyword == null) return is;
		
		long tagBit = keyword.getTagBit();
		// 사전의 tag 정보와 포함여부 tag 의 교집합 구함.
		long result = tagBit & tag.getBit();
		
		if(result > 0){
			return true;
		}

		return is;
	}
	
	public static boolean isIrrRule(Keyword keyword, IrrRule[] rules){
		if (keyword == null || ArrayUtils.isEmpty(rules)) {
            return false;
        }
		
        for (final IrrRule rule : rules) {
            if (isIrrRule(keyword, rule)) {
                return true;
            }
        }
        return false;
	}
	
	public static boolean isIrrRule(Keyword keyword, IrrRule rule){
		boolean is = false;
		
		if(keyword == null) return is;
		if(rule == null) return is;
		
		String r = keyword.getIrrRule();
		if (r == null) {
            return is;
        }
		
		if(r.equals(rule.getName())){
			is = true;
		}
		
		return is;
	}
	
	public static boolean isIrrRule(Keyword keyword){
		boolean is = false;
		
		if(keyword == null) return is;
		
		String r = keyword.getIrrRule();
		if (r != null) {
            is = true;
        }
		
		return is;
	}
	
	
	public static POSTag getMatchPOSTag(long keywordTagBits, POSTag tag){
		
		POSTag resultTag = null;
		
		long matchBit = keywordTagBits & tag.getBit();
		
		if(matchBit > 0){
			resultTag = Config.bitPosTags.get(matchBit);
		}
		
		return resultTag;
	}
	
	public static final IrrRule[] IRR_RULES = new IrrRule[] { IrrRule.irrL, IrrRule.irrb, IrrRule.irrd, IrrRule.irrh, IrrRule.irrl, IrrRule.irrs, IrrRule.irru };

	public static final char EMPTY_JONGSEONG = '\0';
	
	public static final char[] CHOSEONG = { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
			'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };

	public static final char[] JUNGSEONG = { 'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ',
			'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ' };

	public static final char[] JONGSEONG = { EMPTY_JONGSEONG, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ',
			'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };
	
	public static final char[] NO_JONGSEONG = { EMPTY_JONGSEONG };
	
	private static final Map<Character, Integer> CHOSEONG_MAP;
	private static final Map<Character, Integer> JUNGSEONG_MAP;
	private static final Map<Character, Integer> JONGSEONG_MAP;
	
	static {
		CHOSEONG_MAP = new HashMap<Character, Integer>();
		
		for(int i=0,len = CHOSEONG.length;i<len;i++){
			char c = CHOSEONG[i];
			CHOSEONG_MAP.put(c, i);
		}
		JUNGSEONG_MAP = new HashMap<Character, Integer>();
		
		for(int i=0,len = JUNGSEONG.length;i<len;i++){
			char c = JUNGSEONG[i];
			JUNGSEONG_MAP.put(c, i);
		}
		JONGSEONG_MAP = new HashMap<Character, Integer>();
		
		for(int i=0,len = JONGSEONG.length;i<len;i++){
			char c = JONGSEONG[i];
			JONGSEONG_MAP.put(c, i);
		}
	}
	
	private static final int JUNG_JONG = JUNGSEONG.length * JONGSEONG.length;
	
	/**
	 * 한글 한글자를 초성/중성/종성의 배열로 만들어 반환한다.
	 * @param c 분리 할 한글 문자
	 * @return 초/중/종성 분리 된 char 배열 
	 */
	public static char[] decompose(char c) {
		char[] result = null;

		//한글이 아닌 경우 [0]에 그대로 리턴. 자음/모음만 있는 경우도 그대로 리턴
		if (c < KOR_START || c > KOR_END)
			return new char[] { c };

		c -= KOR_START;

		char choseong = CHOSEONG[c / JUNG_JONG];
		c = (char) (c % JUNG_JONG);

		char jungseong = JUNGSEONG[c / JONGSEONG.length];

		char jongseong = JONGSEONG[c % JONGSEONG.length];

		if (jongseong != 0) {
			result = new char[] { choseong, jungseong, jongseong };
		} else {
			result = new char[] { choseong, jungseong, EMPTY_JONGSEONG };
		}
		return result;
	}
	
	
	public static char compound(char choseong, char jungseong) {
	
		return compound(choseong, jungseong, EMPTY_JONGSEONG);
	}
	
	public static char compound(char choseong, char jungseong, char jongseong) {
	
		int first = CHOSEONG_MAP.get(choseong);
		int middle = JUNGSEONG_MAP.get(jungseong);
		int last = JONGSEONG_MAP.get(jongseong);
		
		char c = (char) (KOR_START + (first * JUNG_JONG) + (middle * JONGSEONG.length) + last);
		return c;
	}
	
	
	public static char[] removeElement(final char[] array, final char[] element) {
		char[] results = ArrayUtils.clone(array);
		
		if(ArrayUtils.isEmpty(element)){
			return results; 
		}
		
		for(char c : element){
			final int index = ArrayUtils.indexOf(results, c);
			
			if(index > -1){
				results = ArrayUtils.remove(results, index);
			}
		}
		
        return results;
    }
	
}
