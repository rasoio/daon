package daon.analysis.ko.model;

import java.util.ArrayList;
import java.util.List;

import daon.analysis.ko.dict.rule.MergeRule;
import daon.analysis.ko.util.Utils;

public class MergeInfo {
	
	private Keyword prev;
	private Keyword next;
	
	private String prevWord;
	private String nextWord;
	
	private char[] prevEnd;
	private char[] prevEnd2;
	
	private char[] nextStart;

	public MergeInfo(Keyword prev, Keyword next){
		this.prev = prev;
		this.next = next;
		
		prevWord = prev.getWord();
		nextWord = next.getWord();
		
		prevEnd = Utils.getCharAtDecompose(prev, -1);
		prevEnd2 = Utils.getCharAtDecompose(prev, -2);
		
		nextStart = Utils.getCharAtDecompose(next, 0);
	}
	
	public Keyword getPrev() {
		return prev;
	}

	public void setPrev(Keyword prev) {
		this.prev = prev;
	}

	public Keyword getNext() {
		return next;
	}

	public void setNext(Keyword next) {
		this.next = next;
	}

	public String getPrevWord() {
		return prevWord;
	}

	public void setPrevWord(String prevWord) {
		this.prevWord = prevWord;
	}

	public String getNextWord() {
		return nextWord;
	}

	public void setNextWord(String nextWord) {
		this.nextWord = nextWord;
	}

	public char[] getPrevEnd() {
		return prevEnd;
	}

	public void setPrevEnd(char[] prevEnd) {
		this.prevEnd = prevEnd;
	}

	public char[] getPrevEnd2() {
		return prevEnd2;
	}

	public void setPrevEnd2(char[] prevEnd2) {
		this.prevEnd2 = prevEnd2;
	}

	public char[] getNextStart() {
		return nextStart;
	}

	public void setNextStart(char[] nextStart) {
		this.nextStart = nextStart;
	}
	
	
	
}
