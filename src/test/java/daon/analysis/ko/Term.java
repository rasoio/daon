package daon.analysis.ko;

/**
 * Analyzed token with morphological data from its dictionary.
 */
public class Term {

	private final Word word;

	private final int offset;
	private final int length;

	public Term(Word word, int offset, int length) {
		this.word = word;
		this.offset = offset;
		this.length = length;
	}

	public Word getWord() {
		return word;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "Term [word=" + word + ", offset=" + offset + ", length=" + length + "]";
	}
	
}
