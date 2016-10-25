package daon.analysis.ko;

import java.util.ArrayList;
import java.util.List;

public class Keyword {

	private long seq;
	private String word;
	private List<String> attr;
	private long tf;
	private List<Keyword> subWords;

	public Keyword(){
		super();
	}
	
	public Keyword(String word, String attr) {
		super();
		this.word = word;
		this.attr = new ArrayList<String>();
		this.attr.add(attr);
		this.tf = 0;
	}
	
	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
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

	public List<Keyword> getSubWords() {
		return subWords;
	}

	public void setSubWords(List<Keyword> subWords) {
		this.subWords = subWords;
	}

	@Override
	public String toString() {
		return "Keyword [seq=" + seq + ", word=" + word + ", attr=" + attr + ", tf=" + tf + ", subWords=" + subWords
				+ "]";
	}
	

}
