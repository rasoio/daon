package daon.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daon.core.config.POSTag;
import daon.core.data.Eojeol;
import daon.core.data.Morpheme;

public class Utils {

    /**
     * http://jrgraphix.net/r/Unicode/AC00-D7AF
     */
    //한글 시작 문자
    public final static int KOR_START = 0xAC00;
    //한글 종료 문자
    public final static int KOR_END = 0xD7A3;

    /**
     * http://jrgraphix.net/r/Unicode/3130-318F
     */
    public final static int JAMO_START = 0x3130;

    public final static int JAMO_END = 0x318F;

    public final static int HANJA_START = 0x4E00;
    public final static int HANJA_END = 0x9FFF;

    public final static int INDEX_NOT_FOUND = -1;


    /**
     * 맨앞부터는 0,1,2
     * 맨뒤부터는 -1,-2,-3
     *
     * @param word word
     * @param idx decomposed index
     * @return decompose value
     */
    public static char[] getCharAtDecompose(String word, int idx) {

        if (word == null) return new char[]{};

        int len = word.length();

        //마지막 문자 가져오기
        if (idx <= -1) {
            idx = len + idx;

            if (idx < 0) {
                return new char[]{};
            }
        }

        if (idx >= len) {
            return new char[]{};
        }

        char c = word.charAt(idx);

        char[] lastchar = decompose(c);

        return lastchar;
    }

    public static char[] getFirstChar(String word) {

        return getCharAtDecompose(word, 0);
    }

    public static char[] getLastChar(String word) {

        return getCharAtDecompose(word, -1);
    }

    public static int indexOf(final char[] array, final char valueToFind) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }

        for (int i = 0; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * @param c decomposed value
     * @param array match value's
     * @param findIdx 찾을 idx. 0 : 초성, 1 : 중성, 2 : 종성
     * @return isMatch
     */
    public static boolean isMatch(char[] c, char[] array, int findIdx) {
        boolean isMatched = false;
        int chk = findIdx < 1 ? 1 : findIdx;
        int len = c.length;

        if (len == 0) {
            return isMatched;
        }

        // 분리 내용이 있는 경우 있을 경우
        if (len > chk) {
            int idx = indexOf(array, c[findIdx]);

            if (idx > -1) {
                isMatched = true;
            }
        }

        return isMatched;
    }

    public static boolean isMatch(char[] c, char[] choseong, char[] jungseong, char[] jongseong) {
        boolean isMatched = false;

        if (isMatch(c, choseong, 0)
                && isMatch(c, jungseong, 1)
                && isMatch(c, jongseong, 2)) {
            isMatched = true;
        }

        return isMatched;
    }

    public static boolean isMatch(char[] c, char[] choseong, char[] jungseong) {
        boolean isMatched = false;

        if (isMatch(c, choseong, 0)
                && isMatch(c, jungseong, 1)) {
            isMatched = true;
        }

        return isMatched;
    }


    public static boolean isMatch(char[] c, char choseong) {
        boolean isMatched = false;

        if (c[0] == choseong) {
            isMatched = true;
        }

        return isMatched;
    }

    public static boolean isMatch(char[] c, char choseong, char jungseong) {
        boolean isMatched = false;

        if ((c[0] == choseong)
                && (c[1] == jungseong)) {
            isMatched = true;
        }

        return isMatched;
    }

    public static boolean startsWith(String word, char[] choseong, char[] jungseong, char[] jongseong) {

        char[] c = getFirstChar(word);

        return isMatch(c, choseong, jungseong, jongseong);
    }

    public static boolean startsWith(String word, char[] choseong, char[] jungseong) {

        char[] c = getFirstChar(word);

        return isMatch(c, choseong, jungseong);
    }

    public static boolean startsWith(String word, String[] prefixes) {
        if (word == null) return false;

        for (final String searchString : prefixes) {
            if (word.startsWith(searchString)) {
                return true;
            }
        }

        return false;
    }

    public static boolean startsWith(String word, String prefix) {
        return startsWith(word, new String[]{prefix});
    }

    public static boolean startsWithChoseong(String word, char[] choseong) {

        char[] c = getFirstChar(word);

        return isMatch(c, choseong, 0);
    }

    public static boolean startsWithChoseong(String word, char choseong) {
        return startsWithChoseong(word, new char[]{choseong});
    }

    public static boolean startsWithChoseong(String word) {
        return startsWithChoseong(word, CHOSEONG);
    }

    public static boolean startsWithJongseong(String word, char[] jongseong) {
        boolean isMatched = false;

        char[] c = getFirstChar(word);

        //종성만 있는 경우
        if (c.length == 1) {
            int idx = indexOf(jongseong, c[0]);

            if (idx > -1) {
                isMatched = true;
            }
        }

        return isMatched;
    }

    public static boolean startsWithJongseong(String word, char jongseong) {
        return startsWithJongseong(word, new char[]{jongseong});
    }

    public static boolean startsWithJongseong(String word) {
        return startsWithJongseong(word, JONGSEONG);
    }


    public static boolean endWith(String word, String[] suffixes) {
        if (word == null) return false;

        for (final String searchString : suffixes) {
            if (word.startsWith(searchString)) {
                return true;
            }
        }

        return false;
    }

    public static boolean endWith(String word, String suffix) {

        return endWith(word, new String[]{suffix});
    }

    public static boolean endWith(String word, char[] choseong, char[] jungseong, char[] jongseong) {

        char[] c = getLastChar(word);

        return isMatch(c, choseong, jungseong, jongseong);
    }

    public static boolean endWith(String word, char[] choseong, char[] jungseong) {

        char[] c = getLastChar(word);

        return isMatch(c, choseong, jungseong);
    }

    public static boolean endWithChoseong(String word, char[] choseong) {
        char[] c = getLastChar(word);

        return isMatch(c, choseong, 0);
    }

    public static boolean endWithChoseong(String word, char choseong) {
        return endWithChoseong(word, new char[]{choseong});
    }

    public static boolean endWithJungseong(String word, char[] jungseong) {
        char[] c = getLastChar(word);

        return isMatch(c, jungseong, 1);
    }

    public static boolean endWithJungseong(String word, char jungseong) {
        return endWithJungseong(word, new char[]{jungseong});
    }

    public static boolean endWithJongseong(String word, char[] jongseong) {
        char[] c = getLastChar(word);

        return isMatch(c, jongseong, 2);
    }

    public static boolean endWithJongseong(String word, char jongseong) {
        return endWithJongseong(word, new char[]{jongseong});
    }

    public static boolean endWithNoJongseong(String word) {

        char[] lc = getLastChar(word);

        return isMatch(lc, NO_JONGSEONG, 2);
    }

    public static boolean isLength(String word, int len) {
        boolean is = false;

        if (word == null) return is;

        if (word.length() == len) {
            is = true;
        }

        return is;
    }

    public static final char EMPTY_JONGSEONG = '\0';

    public static final char[] CHOSEONG = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
            'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    public static final char[] JUNGSEONG = {'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ',
            'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'};

    public static final char[] JONGSEONG = {EMPTY_JONGSEONG, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ',
            'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    public static final char[] NO_JONGSEONG = {EMPTY_JONGSEONG};

    private static final Map<Character, Integer> CHOSEONG_MAP;
    private static final Map<Character, Integer> JUNGSEONG_MAP;
    private static final Map<Character, Integer> JONGSEONG_MAP;

    static {
        CHOSEONG_MAP = new HashMap<Character, Integer>();

        for (int i = 0, len = CHOSEONG.length; i < len; i++) {
            char c = CHOSEONG[i];
            CHOSEONG_MAP.put(c, i);
        }
        JUNGSEONG_MAP = new HashMap<Character, Integer>();

        for (int i = 0, len = JUNGSEONG.length; i < len; i++) {
            char c = JUNGSEONG[i];
            JUNGSEONG_MAP.put(c, i);
        }
        JONGSEONG_MAP = new HashMap<Character, Integer>();

        for (int i = 0, len = JONGSEONG.length; i < len; i++) {
            char c = JONGSEONG[i];
            JONGSEONG_MAP.put(c, i);
        }
    }

    private static final int JUNG_JONG = JUNGSEONG.length * JONGSEONG.length;

    /**
     * 한글 한글자를 초성/중성/종성의 배열로 만들어 반환한다.
     *
     * @param c 분리 할 한글 문자
     * @return 초/중/종성 분리 된 char 배열
     */
    public static char[] decompose(char c) {
        char[] result = null;

        //한글이 아닌 경우 [0]에 그대로 리턴. 자음/모음만 있는 경우도 그대로 리턴
        if (c < KOR_START || c > KOR_END)
            return new char[]{c};

        c -= KOR_START;

        char choseong = CHOSEONG[c / JUNG_JONG];
        c = (char) (c % JUNG_JONG);

        char jungseong = JUNGSEONG[c / JONGSEONG.length];

        char jongseong = JONGSEONG[c % JONGSEONG.length];

        if (jongseong != 0) {
            result = new char[]{choseong, jungseong, jongseong};
        } else {
            result = new char[]{choseong, jungseong, EMPTY_JONGSEONG};
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


    public static long hashCode(String string){

        long h = 98764321261L;
        int l = string.length();
        char[] chars = string.toCharArray();

        for (int i = 0; i < l; i++) {
            h = 31 * h + chars[i];
        }
        return h;

    }

    public static boolean containsTag(long sourceBit, long targetBit){
        boolean is = false;

        // 사전의 tag 정보와 포함여부 tag 의 교집합 구함.
        long result = sourceBit & targetBit;

        if(result > 0){
            is = true;
        }

        return is;
    }

    public static boolean containsTag(long sourceBit, POSTag targetTag){
        long targetBit = targetTag.getBit();
        return containsTag(sourceBit, targetBit);
    }

    public static boolean containsTag(POSTag sourceTag, POSTag targetTag){
        long sourceBit = sourceTag.getBit();
        return containsTag(sourceBit, targetTag);
    }

    public static int getSeq(POSTag tag){
        int seq = 0;

        if(tag == POSTag.SN){
            seq = 1;
        }else if(tag == POSTag.SL || tag == POSTag.SH){
            seq = 2;
        }
        return seq;
    }

    public static int getSeq(String tag){

        return getSeq(POSTag.valueOf(tag));
    }

    public static int getIdx(String tag){

        return POSTag.valueOf(tag).getIdx();
    }


    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Performs the logic for the {@code split} and
     * {@code splitPreserveAllTokens} methods that do not return a
     * maximum array length.
     *
     * @param str  the String to parse, may be {@code null}
     * @param separatorChar the separate character
     * @return an array of parsed Strings, {@code null} if null String input
     */
    private static String[] split(final String str, final char separatorChar) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        final List<String> list = new ArrayList<String>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 어절 구분 : 줄바꿈 문자
     * 어절-형태소 간 구분 : ' - '
     * 형태소 간 구분 : 공백(스페이스) 문자
     * 형태소 내 단어-태그 구분 : '/'
     * @param eojeols 파싱할 어절 문자열
     * @return 분석 결과
     * @throws Exception 파싱 실패 에러
     */
    public static List<Eojeol> parse(String eojeols) throws Exception{

        if(eojeols == null || eojeols.isEmpty()){
            throw new Exception("값이 없습니다.");
        }

        List<Eojeol> results = new ArrayList<>();

        String[] lines = eojeols.split("\\n");

        if(lines.length == 0){
            throw new Exception("어절 구분 분석 결과가 없습니다. (어절 구분자 줄바꿈 문자)");
        }

        for(int i=0, len = lines.length; i< len; i++){
            String line = lines[i];

            String[] surfaceAndMorphs = line.split("\\s+[-]\\s+");

            if(surfaceAndMorphs.length == 0){
                throw new Exception("어절-형태소 간 구분 값(' - ')이 없습니다.");
            }

            if(surfaceAndMorphs.length > 2){
                throw new Exception("어절-형태소 간 구분 값(' - ')은 한개만 있어야 됩니다.");
            }

            String surface = surfaceAndMorphs[0];

            String morph = surfaceAndMorphs[1];

            Eojeol eojeol = new Eojeol();
            eojeol.setSeq(i);
            eojeol.setSurface(surface);

            List<Morpheme> morphemes = parseMorpheme(morph);

            eojeol.setMorphemes(morphemes);

            results.add(eojeol);
        }

        return results;
    }

    public static List<Morpheme> parseMorpheme(String m) throws Exception {

        if(m == null || m.isEmpty()){
            throw new Exception("값이 없습니다.");
        }

        List<Morpheme> results = new ArrayList<>();

        String[] morphes = m.split("\\s+");

        if(morphes.length == 0){
            throw new Exception("형태소 간 구분자 ' '가 없습니다.");
        }

        for(int i=0, len = morphes.length; i < len; i++){
            String morph = morphes[i];

            //형태소 내 단어-태그 구분자 '/' 분리
            String[] wordInfo = morph.split("[/](?=[A-Z]{2,3}$)");

            if(wordInfo.length != 2){
                throw new Exception("형태소 내 단어-태그 구분자('/')가 잘못 되었습니다.");
            }

            String word = wordInfo[0];
            String tag = wordInfo[1];

            try {
                tag = POSTag.valueOf(tag).getName();
            }catch (Exception e){
                throw new Exception(tag + " <= 태그값이 잘못되었습니다.");
            }

            Morpheme morpheme = new Morpheme(i, word, tag);

            results.add(morpheme);
        }

        if(results.size() == 0){
            throw new Exception("형태소 구분 분석 결과가 없습니다. (형태소 구분자 공백 문자)");
        }

        return results;
    }

    public static long makeTagBit(List<String> tags){
        long bit = -1;
        if(tags != null && !tags.isEmpty()) {
            bit = 0;
            for (String tagName : tags) {
                POSTag tag = POSTag.valueOf(tagName);
                bit |= tag.getBit();
            }
        }
        return bit;
    }

    public static long makeTagBit(String[] tags) {
        long bit = -1;
        if (tags != null && tags.length > 0) {
            bit = 0;
            for (String tagName : tags) {
                POSTag tag = POSTag.valueOf(tagName);
                bit |= tag.getBit();
            }
        }
        return bit;
    }
}
