package daon.core.util;


import daon.core.config.CharType;

public class CharTypeChecker {

    public static final CharType[] charTypeTable;

    static {
        CharType[] tab = new CharType[256];
        for (int i = 0; i < 256; i++) {
            CharType code = CharType.ETC;

            if (Character.isLowerCase(i)) {
                code = CharType.ALPHA; //CharType.LOWER;
            } else if (Character.isUpperCase(i)) {
                code = CharType.ALPHA; //CharType.UPPER;
            } else if (Character.isDigit(i)) {
                code = CharType.DIGIT;
            } else if (Character.isWhitespace(i)) { // 32 (spacebar), 9 (tab), 10 (new line), 13 (return)
                code = CharType.SPACE;
            }

            tab[i] = code;
        }

        charTypeTable = tab;
    }

    public static CharType charType(int ch) {

        if (ch < charTypeTable.length) {
            return charTypeTable[ch];
        }

        // 한글 타입 체크
        if (ch >= Utils.KOR_START && ch <= Utils.KOR_END) {
            return CharType.KOREAN;
        }

        // 자모 타입 체크
        if (ch >= Utils.JAMO_START && ch <= Utils.JAMO_END) {
            return CharType.JAMO;
        }

        // 한자 타입 체크
        if (ch >= Utils.HANJA_START && ch <= Utils.HANJA_END) {
            return CharType.HANJA;
        }

        return getType(ch);
    }

    private static CharType getType(int ch) {
        int type = Character.getType(ch);

        switch (type) {
            case Character.UPPERCASE_LETTER:
                return CharType.UPPER;
            case Character.LOWERCASE_LETTER:
                return CharType.LOWER;

            case Character.TITLECASE_LETTER:
            case Character.MODIFIER_LETTER:
            case Character.NON_SPACING_MARK:
            case Character.ENCLOSING_MARK: // depends what it encloses?
            case Character.COMBINING_SPACING_MARK:
                return CharType.ALPHA;

            case Character.OTHER_LETTER:
                return CharType.HANJA;

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

    public static boolean isKorean(CharType type) {
        return (type.getBit() & CharType.KOREAN.getBit()) != 0;
    }

    public static boolean isSpace(CharType type) {
        return (type.getBit() & CharType.SPACE.getBit()) != 0;
    }

    public static boolean isAlpha(CharType type) {
        return (type.getBit() & CharType.ALPHA.getBit()) != 0;
    }

    public static boolean isDigit(CharType type) {
        return (type.getBit() & CharType.DIGIT.getBit()) != 0;
    }
}
