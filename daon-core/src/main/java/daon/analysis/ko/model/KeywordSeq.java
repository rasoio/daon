package daon.analysis.ko.model;

import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;

import java.util.Arrays;

public class KeywordSeq implements Comparable<KeywordSeq> {

    private IntsRef input;

    private final int[] seqs;

    private long weight;

    public KeywordSeq(String word, int... seqs) {

        IntsRefBuilder scratch = new IntsRefBuilder();
        scratch.grow(word.length());
        scratch.setLength(word.length());

        for (int i = 0; i < word.length(); i++) {
            scratch.setIntAt(i, (int) word.charAt(i));
        }

        input = scratch.get();

        this.seqs = seqs;
    }

    public KeywordSeq(Keyword keyword) {

        this(keyword.getWord(), keyword.getSeq());
    }

    public IntsRef getInput() {
        return input;
    }

    public void clearInput() {
        input = null;
    }

    public int[] getSeqs() {
        return seqs;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(KeywordSeq other) {
        return this.getInput().compareTo(other.getInput());
    }


    @Override
    public String toString() {
        return "KeywordSeq{" +
                "input=" + input +
                ", seqs=" + Arrays.toString(seqs) +
                '}';
    }
}
