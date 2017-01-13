package daon.analysis.ko.model;

import daon.analysis.ko.util.Utils;

public class PrevInfo {
	
	private Keyword prev;
	
	private String prevWord;
	
	private char[] prevEnd;
	

	public PrevInfo(Keyword prev){
		this.prev = prev;
		
		prevWord = prev.getWord();
		
		prevEnd = Utils.getCharAtDecompose(prev, -1);
	}
	
	public Keyword getPrev() {
		return prev;
	}

	public void setPrev(Keyword prev) {
		this.prev = prev;
	}

	public String getPrevWord() {
		return prevWord;
	}

	public void setPrevWord(String prevWord) {
		this.prevWord = prevWord;
	}

	public char[] getPrevEnd() {
		return prevEnd;
	}

	public void setPrevEnd(char[] prevEnd) {
		this.prevEnd = prevEnd;
	}
	
}
