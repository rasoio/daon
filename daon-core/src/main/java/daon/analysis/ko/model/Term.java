package daon.analysis.ko.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
    private final Keyword[] keywords;

    private Keyword first;

    private Keyword last;

    private final ExplainInfo explainInfo;

    public Term(int offset, int length, String surface, ExplainInfo explainInfo, Keyword... keywords) {
        this.offset = offset;
        this.length = length;
        this.surface = surface;
        this.keywords = keywords;
        this.explainInfo = explainInfo;

        int size = keywords.length;

        //TODO keywords가 없을때 처리 방안
        if(size > 0) {
            first = keywords[0];
            last = keywords[size - 1];
        }
    }

    public Keyword[] getKeywords() {
        return keywords;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public Keyword getFirst() {
        return first;
    }

    public Keyword getLast() {
        return last;
    }

    public ExplainInfo getExplainInfo() {
        return explainInfo;
    }

    public String getSurface() {
        return surface;
    }

    public List<Integer> getSeqs(){

        List<Integer> seqs = new ArrayList<>();

        Stream.of(keywords).forEach(keyword -> {
            seqs.add(keyword.getSeq());
        });

        return seqs;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", length=" + length +
                ", keywords=" + Arrays.toString(keywords) +
                ", explain=" + explainInfo +
                '}';
    }
}
