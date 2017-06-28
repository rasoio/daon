package daon.analysis.ko.model;

import daon.analysis.ko.config.MatchType;
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

    private final MatchType matchType;

    private Arc arc;

    private float freq;

    public Term(int offset, int length, String surface, MatchType matchType, float freq, Keyword... keywords) {
        this.offset = offset;
        this.length = length;
        this.surface = surface;
        this.keywords = keywords;
        this.matchType = matchType;
        this.freq = freq;
    }

    public float getFreq() {
        return freq;
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

    public MatchType getMatchType() {
        return matchType;
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

    public Arc getArc() {
        return arc;
    }

    public void setArc(Arc arc) {
        this.arc = arc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        if (offset != term.offset) return false;
        if (length != term.length) return false;
        if (!surface.equals(term.surface)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(keywords, term.keywords);
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + length;
        result = 31 * result + surface.hashCode();
        result = 31 * result + Arrays.hashCode(keywords);
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", length=" + length +
                ", freq=" + String.format("%.10f", freq)  +
                ", keywords=" + Arrays.toString(keywords) +
                ", matchType=" + matchType +
//                ", arc=" + arc +
                '}';
    }
}
