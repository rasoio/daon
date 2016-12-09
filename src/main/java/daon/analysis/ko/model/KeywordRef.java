package daon.analysis.ko.model;

import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;

public class KeywordRef {
	
	private final IntsRef input;
	
	private final long[] wordIds;

	public KeywordRef(String word, long... wordIds){

		IntsRefBuilder scratch = new IntsRefBuilder();
		scratch.grow(word.length());
		scratch.setLength(word.length());
		
		for (int i = 0; i < word.length(); i++) {
			scratch.setIntAt(i, (int) word.charAt(i));
		}

		input = scratch.get();

		this.wordIds = wordIds;
	}
	
	public KeywordRef(Keyword keyword, long... wordIds){

		this(keyword.getWord(), wordIds);
	}

	public IntsRef getInput() {
		return input;
	}

	public long[] getWordIds() {
		return wordIds;
	}
	
}
