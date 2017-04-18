package daon.analysis.ko.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
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


    private final String surface;

    //연결 결과
    private final List<Keyword> keywords;

    private final Keyword last;
    private final Keyword first;


    private final ExplainInfo explainInfo;

    public CandidateTerm(int offset, int length, String surface, List<Keyword> keywords, ExplainInfo explainInfo) {
        this.offset = offset;
        this.length = length;
        this.surface = surface;
        this.keywords = keywords;
        this.explainInfo = explainInfo;


        int size = keywords.size();

        first = keywords.get(0);
        last = keywords.get(size - 1);
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

    public Keyword getLast() {
        return last;
    }

    public Keyword getFirst() {
        return first;
    }

    public ExplainInfo getExplainInfo() {
        return explainInfo;
    }

    public String getSurface() {
        return surface;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", length=" + length +
                ", keywords=" + keywords +
                ", explain=" + explainInfo +
                '}';
    }
}
