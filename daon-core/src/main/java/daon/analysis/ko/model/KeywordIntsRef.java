package daon.analysis.ko.model;

import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;

import java.io.Serializable;
import java.util.Arrays;


public class KeywordIntsRef implements Comparable<KeywordIntsRef>, Serializable {

    private IntsRef input;

    private final int[] seqs;

    private long freq;

    public KeywordIntsRef(String word, int[] seqs) {

        IntsRefBuilder scratch = new IntsRefBuilder();
        scratch.grow(word.length());
        scratch.setLength(word.length());

        for (int i = 0; i < word.length(); i++) {
            scratch.setIntAt(i, (int) word.charAt(i));
        }

        input = scratch.get();

        this.seqs = seqs;
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

    public long getFreq() {
        return freq;
    }

    public void setFreq(long freq) {
        this.freq = freq;
    }

    @Override
    public int compareTo(KeywordIntsRef other) {
        return this.getInput().compareTo(other.getInput());
    }


    @Override
    public String toString() {
        return "KeywordIntsRef{" +
                "input=" + input +
                ", seqs=" + Arrays.toString(seqs) +
                '}';
    }
}
