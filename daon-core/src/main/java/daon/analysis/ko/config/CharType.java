package daon.analysis.ko.config;

/**
 * The charactor types
 */
public enum CharType {

    LOWER("LOWER", 1l << 0), // 소문자 00001
    UPPER("UPPER", 1l << 1), // 대문자 00010

    ALPHA("ALPHA", LOWER.getBit() | UPPER.getBit()), // 영문 전체 00011

    DIGIT("DIGIT", 1l << 2), // 숫자 // 00100
    KOREAN("KOREAN", 1l << 3), // 국문 // 01000

    CHAR("CHAR", ALPHA.getBit() | DIGIT.getBit() | KOREAN.getBit()), // 문자 // 01111

    SPACE("SPACE", 1l << 4), // 공백 // 010000
    COMMA("COMMA", 1l << 5), // 콤마 // 100000

    ETC("ETC", 1l << 99), // 기타 특수기호
    ;

    private String name;
    private long bit;

    CharType(String name, long bit) {
        this.name = name;
        this.bit = bit;
    }

    public String getName() {
        return name;
    }
    public long getBit() {
        return bit;
    }
}
