package daon.analysis.ko;

import java.util.ArrayList;
import java.util.List;

public class Word {

	private String word;
	private List<String> attr;
	private long tf;
	private List<Word> subWords;

	public Word(){
		super();
	}
	
	public Word(String word, String attr) {
		super();
		this.word = word;
		this.attr = new ArrayList<String>();
		this.attr.add(attr);
		this.tf = 0;
	}
	
	public Word(String word, List<String> attr, long tf, List<Word> subWords) {
		super();
		this.word = word;
		this.attr = attr;
		this.tf = tf;
		this.subWords = subWords;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public List<String> getAttr() {
		return attr;
	}

	public void setAttr(List<String> attr) {
		this.attr = attr;
	}

	public long getTf() {
		return tf;
	}

	public void setTf(long tf) {
		this.tf = tf;
	}

	public List<Word> getSubWords() {
		return subWords;
	}

	public void setSubWords(List<Word> subWords) {
		this.subWords = subWords;
	}

	@Override
	public String toString() {
		return "Word [term=" + word + ", attr=" + attr + ", tf=" + tf + ", subTerms=" + subWords + "]";
	}

}
