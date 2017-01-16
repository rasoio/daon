package daon.analysis.ko.model;

import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;

public class KeywordRef implements Comparable<KeywordRef> {

    private IntsRef input;

    private final Keyword[] keywords;

    public KeywordRef(String word, Keyword... keywords) {

        IntsRefBuilder scratch = new IntsRefBuilder();
        scratch.grow(word.length());
        scratch.setLength(word.length());

        for (int i = 0; i < word.length(); i++) {
            scratch.setIntAt(i, (int) word.charAt(i));
        }

        input = scratch.get();

        this.keywords = keywords;
    }

    public KeywordRef(Keyword keyword) {

        this(keyword.getWord(), keyword);
    }

    public IntsRef getInput() {
        return input;
    }

    public void clearInput() {
        input = null;
    }

    public Keyword[] getKeywords() {
        return keywords;
    }

    @Override
    public int compareTo(KeywordRef other) {
        return this.getInput().compareTo(other.getInput());
    }
}
