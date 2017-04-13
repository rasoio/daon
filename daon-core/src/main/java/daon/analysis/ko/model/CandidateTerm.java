package daon.analysis.ko.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 분석 결과
 */
public class CandidateTerm {


    private Logger logger = LoggerFactory.getLogger(CandidateTerm.class);

    /**
     * 분석 결과 offset 위치 정보
     */
    private final int offset;

    /**
     * word 길이
     */
    private final int length;

//    private final String surface;

    //연결 결과
    private final List<Keyword> keywords;

    public CandidateTerm(int offset, int length, List<Keyword> keywords) {
        this.offset = offset;
        this.length = length;
        this.keywords = keywords;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", length=" + length +
                ", keywords=" + keywords +
                '}';
    }
}
