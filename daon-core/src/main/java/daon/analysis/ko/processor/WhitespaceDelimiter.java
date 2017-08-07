package daon.analysis.ko.processor;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.util.CharTypeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mac on 2017. 5. 18..
 */
public class WhitespaceDelimiter {

    private Logger logger = LoggerFactory.getLogger(WhitespaceDelimiter.class);

    public static WhitespaceDelimiter create(char[] texts) {
        return new WhitespaceDelimiter(texts);
    }


    private char[] texts;

    private int length;

    public int end;
    public int current;

    /**
     * Indicates the end of iteration
     */
    public static final int DONE = -1;

    public WhitespaceDelimiter(char[] texts) {
        this.texts = texts;

        current = end = 0;
        length = texts.length;
    }

    public void reset() {
        current = end = length = 0;
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

        //앞 공백은 계속 스킵 처리
        while (current < length && isSpace(texts[current])) {
            current++;
        }

        if (current >= length) {
            return end = DONE;
        }

        // end 를 current 부터 1씩 증가.
        for (end = current + 1; end < length; end++) {
//            logger.info("c : {}, type : {}, current : {}, end : {}", c, type.getName(), current, end);

            // 공백 기준 분리
            if (isSpace(texts[end])) {
                break;
            }
        }


        return end;
    }

    public boolean isSpace(int c) {
        CharType type = CharTypeChecker.charType(c);
        return CharTypeChecker.isSpace(type);
    }

}
