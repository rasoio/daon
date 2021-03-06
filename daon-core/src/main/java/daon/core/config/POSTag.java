package daon.core.config;

/**
 * part of speech tag
 * <p>
 * 한글 품사
 * 9품사
 * 체언 ( 명사, 대명사, 수사 )
 * 용언 ( 동사, 형용사 )
 * 관형사
 * 부사
 * 감탄사
 * 조사
 */
public enum POSTag {
    FIRST("FIRST", 0, 0L),   // 시작 태그
    LAST("LAST", 0, 0L),   // 종료 태그
    UNKNOWN("UNKNOWN", 0, 1L << 50),   // UNKNOWN 태그

    // 체언 ( 명사, 대명사, 수사 )
    NNG("NNG", 1, 1L), 	// 일반 명사
    NNP("NNP", 2, 1L << 1), 	// 고유 명사
    NNB("NNB", 3, 1L << 2), 	// 의존 명사
    NR("NR", 4, 1L << 3), 	// 수사
    NP("NP", 5, 1L << 4), 	// 대명사

    // 명사 (명사 + 수사 + 명사 추정 미등록어)
    NN("NN", 6, NNG.getBit() | NNP.getBit() | NNB.getBit() | NR.getBit()),

    // 체언 대표
    N("N", 7, NN.getBit() | NP.getBit() | UNKNOWN.getBit()),

    // 용언
    VV("VV", 8, 1L << 5), 	// 동사
    VA("VA", 9, 1L << 6), 	// 형용사
    VX("VX", 10, 1L << 7), 	// 보조 용언


    VCP("VCP", 11, 1L << 8), 	// 긍정 지정사
    VCN("VCN", 12, 1L << 9), 	// 부정 지정사

    // 서술격 조사 '이다'를 제외한 용언
    VP("VP", 13, VV.getBit() | VA.getBit() | VX.getBit() | VCN.getBit() ),

    // 지정사
    VC("VC", 14, VCN.getBit() | VCP.getBit() ),

    // 용언 대표
    V("V", 16, VP.getBit() | VCP.getBit() ),

    // 관형사
    MM("MM", 17, 1L << 10), 	// 관형사

    // 부사
    MAG("MAG", 18, 1L << 11), 	// 일반 부사
    MAJ("MAJ", 19, 1L << 12), 	// 접속 부사

    // 부사 대표
    MA("MA", 20, MAG.getBit() | MAJ.getBit()),

    // 수식언
    M("M", 21, MM.getBit() | MA.getBit()),

    // 감탄사
    IC("IC", 22, 1L << 13), 	// 감탄사

    // 조사
    JKS("JKS", 23, 1L << 14), 	// 주격 조사
    JKC("JKC", 24, 1L << 15), 	// 보격 조사
    JKG("JKG", 25, 1L << 16), 	// 관형격 조사
    JKO("JKO", 26, 1L << 17), 	// 목적격 조사
    JKB("JKB", 27, 1L << 18), 	// 부사격 조사
    JKV("JKV", 28, 1L << 19), 	// 호격 조사
    JKQ("JKQ", 29, 1L << 20), 	// 인용격 조사
    JX("JX", 30, 1L << 21), 	// 보조사
    JC("JC", 31, 1L << 22), 	// 접속 조사

    // 격조사 대표
    JK("JK", 32, JKS.getBit() | JKC.getBit() | JKG.getBit() | JKO.getBit() | JKB.getBit() | JKV.getBit() | JKQ.getBit() ),

    // 조사 대표
    J("J", 33, JK.getBit() | JX.getBit() | JC.getBit()),

    // 선어말 어미
    EP("EP", 34, 1L << 23), 	// 선어말 어미

    // 어미
    EF("EF", 35, 1L << 24), 	// 종결 어미
    EC("EC", 36, 1L << 25), 	// 연결 어미
    ETN("ETN", 37, 1L << 26), 	// 명사형 전성 어미
    ETM("ETM", 38, 1L << 27), 	// 관형형 전성 어미

    // 전성형 어말 어미 대표
    ET("ET", 39, ETN.getBit() | ETM.getBit()),

    // 어말 어미 대표
    EM("EM", 40, EF.getBit() | EC.getBit() | ET.getBit()),

    // 어미 대표
    E("E", 41, EP.getBit() | EM.getBit()),

    // 접두사
    XPN("XPN", 42, 1L << 28), 	// 체언 접두사

    // 접미사
    XSN("XSN", 43, 1L << 29), 	// 명사 파생접미사
    XSV("XSV", 44, 1L << 30), 	// 동사 파생접미사
    XSA("XSA", 45, 1L << 31), 	// 형용사 파생접미사
    XSB("XSB", 46, 1L << 32), 	// 부사 파생접미사

    // 어근
    XR("XR", 47, 1L << 33), 	// 어근

    // 접미사 대표
    XS("XS", 48, XSN.getBit() | XSV.getBit() | XSA.getBit() | XSB.getBit()),

    // 부호
    SF("SF", 49, 1L << 34), 	// 마침표물음표,느낌표
    SP("SP", 50, 1L << 35), 	// 쉼표,가운뎃점,콜론,빗금
    SS("SS", 51, 1L << 36), 	// 따옴표,괄호표,줄표
    SE("SE", 52, 1L << 37), 	// 줄임표
    SO("SO", 53, 1L << 38), 	// 붙임표(물결,숨김,빠짐)
    SW("SW", 54, 1L << 39), 	// 기타기호 (논리수학기호,화폐기호)

    S("S", 55, SF.getBit() | SP.getBit() | SS.getBit() | SE.getBit() | SO.getBit() | SW.getBit()),

    // 한글 이외
    SL("SL", 56, 1L << 40), 	// 외국어
    SH("SH", 57, 1L << 41), 	// 한자
    SN("SN", 58, 1L << 42), 	// 숫자


    // 분석 불능
    NF("NF", 59, 1L << 43), 	// 체언추정범주
    NV("NV", 60, 1L << 44), 	// 용언추정범주
    NA("NA", 61, 1L << 45), 	// 분석불능범주

    ;


    private String name;
    private int idx;
    private long bit;

    POSTag(String name, int idx, long bit) {
        this.name = name;
        this.idx = idx;
        this.bit = bit;
    }

    public String getName() {
        return name;
    }

    public long getBit() {
        return bit;
    }

    public int getIdx() {
        return idx;
    }
}
