package daon.analysis.ko.model;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 매칭 정보
 */
public class MatchInfo {

    private Logger logger = LoggerFactory.getLogger(daon.analysis.ko.model.MatchInfo.class);

    private MatchType type;

    private int[] seqs;

    private int prevSeq;
    private int seq;
    private int nextSeq;

    private boolean isOuter;

    private int[] EMPTY_SEQ = new int[] {0};

    private MatchInfo(MatchType type) {
        this.type = type;
    }

    public static MatchInfo getInstance(MatchType type){
        return new MatchInfo(type);
    }

    public MatchType getType() {
        return type;
    }


    public int[] getMatchSeqs() {

        switch (type){
            case DICTIONARY:
                return seqs;
            case PREV_CONNECTION:
                return new int[]{prevSeq, seq};
            case NEXT_CONNECTION:
                return new int[]{seq, nextSeq};
        }

        return EMPTY_SEQ;
    }

    public int[] getSeqs() {
        return seqs;
    }

    public int getPrevSeq() {
        return prevSeq;
    }

    public int getSeq() {
        return seq;
    }

    public int getNextSeq() {
        return nextSeq;
    }

    public boolean isOuter() {
        return isOuter;
    }

    public MatchInfo setSeqs(int[] seqs) {
        this.seqs = seqs;
        return this;
    }

    public MatchInfo setPrevSeq(int prevSeq) {
        this.prevSeq = prevSeq;
        return this;
    }

    public MatchInfo setSeq(int seq) {
        this.seq = seq;
        return this;
    }

    public MatchInfo setNextSeq(int nextSeq) {
        this.nextSeq = nextSeq;
        return this;
    }

    public MatchInfo setOuter(boolean outer) {
        isOuter = outer;
        return this;
    }

    @Override
    public String toString() {
        return "MatchInfo{" +
                "type=" + type +
                ", matchSeqs=" + Arrays.toString(getMatchSeqs()) +
                ", isOuter=" + isOuter +
                '}';
    }

    public enum MatchType {
        PREV_CONNECTION,
        DICTIONARY,
        NEXT_CONNECTION,
        UNKNOWN

    }
}
