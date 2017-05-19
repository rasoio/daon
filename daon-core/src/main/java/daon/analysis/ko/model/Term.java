package daon.analysis.ko.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 분석 결과
 */
public class Term {


    private Logger logger = LoggerFactory.getLogger(Term.class);

    /**
     * 분석 결과 offset 위치 정보
     */
    private final int offset;

    /**
     * word 길이
     */
    private final int length;

    /**
     * 표층어
     */
    private final String surface;

    /**
     * 분석 결과
     */
    private final List<Keyword> keywords;

    private int firstSeq = 0;

    private int lastSeq = 0;

    private final ExplainInfo explainInfo;

    public Term(int offset, int length, String surface, List<Keyword> keywords, ExplainInfo explainInfo) {
        this.offset = offset;
        this.length = length;
        this.surface = surface;
        this.keywords = keywords;
        this.explainInfo = explainInfo;

        int size = keywords.size();

        //TODO keywords가 없을때 처리 방안
        if(size > 0) {
            firstSeq = keywords.get(0).getSeq();
            lastSeq = keywords.get(size - 1).getSeq();
        }
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

    public int getLastSeq() {
        return lastSeq;
    }

    public int getFirstSeq() {
        return firstSeq;
    }

    public ExplainInfo getExplainInfo() {
        return explainInfo;
    }

    public String getSurface() {
        return surface;
    }

    public List<Integer> getSeqs(){

        List<Integer> seqs = new ArrayList<>();

        keywords.forEach(keyword -> {
            seqs.add(keyword.getSeq());
        });

        return seqs;
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
