package daon.core.processor;

import daon.core.config.CharType;
import daon.core.util.CharTypeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mac on 2017. 5. 18..
 */
public class WordDelimiter {

    private Logger logger = LoggerFactory.getLogger(WordDelimiter.class);

    public static WordDelimiter create(char[] texts) {
        return new WordDelimiter(texts);
    }


    private char[] texts;

    private int length;
    private int offset = DONE;

    public int end;
    public int current;


    public CharType lastType;

    /**
     * Indicates the end of iteration
     */
    public static final int DONE = -1;

    public WordDelimiter(char[] texts) {
        this.texts = texts;

        current = end = 0;
        length = texts.length;
    }

    public void reset() {
        current = end = length = 0;
        offset = DONE;
    }

    public int getLength() {
        return length;
    }

    public void addLength() {
        this.length++;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int next() {
        // 현재 위치 설정. 이전의 마지막 위치
        current = end;

        if (current == DONE) {
            return DONE;
        }

        if (current >= length) {
            return end = DONE;
        }

        lastType = CharTypeChecker.charType(texts[offset + current]);

        // end 를 current 부터 1씩 증가.
        for (end = current + 1; end < length; end++) {

            CharType type = CharTypeChecker.charType(texts[offset + end]);

            // 마지막 타입과 현재 타입이 다른지 체크, 다르면 stop
            if (isBreak(lastType, type)) {
                break;
            }

            lastType = type;
        }

        return end;
    }

    public static boolean isBreak(CharType lastType, CharType type) {
        //각자 같은 타입이면 false
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









}
