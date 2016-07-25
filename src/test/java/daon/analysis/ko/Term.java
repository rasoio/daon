package daon.analysis.ko;

/**
 * Analyzed token with morphological data from its dictionary.
 */
public class Term {

	private final Keyword word;

	private final int offset;
	private final int length;
	
	private Term prevTerm, nextTerm; // linked list

	public Term(Keyword word, int offset, int length) {
		this.word = word;
		this.offset = offset;
		this.length = length;
	}

	public Keyword getKeyword() {
		return word;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public Term getPrevTerm() {
		return prevTerm;
	}

	public void setPrevTerm(Term prevTerm) {
		this.prevTerm = prevTerm;
	}

	public Term getNextTerm() {
		return nextTerm;
	}

	public void setNextTerm(Term nextTerm) {
		this.nextTerm = nextTerm;
	}
	
	public boolean isGreaterThan(Term t){
		int offsetT = t.getOffset();
		int lengthT = offsetT + t.getLength();
		
		if((offsetT == offset && lengthT < (offset + length)) || (offsetT > offset && lengthT <= (offset + length))){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + ((nextTerm == null) ? 0 : nextTerm.hashCode());
		result = prime * result + offset;
		result = prime * result + ((prevTerm == null) ? 0 : prevTerm.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (length != other.length)
			return false;
		if (nextTerm == null) {
			if (other.nextTerm != null)
				return false;
		} else if (!nextTerm.equals(other.nextTerm))
			return false;
		if (offset != other.offset)
			return false;
		if (prevTerm == null) {
			if (other.prevTerm != null)
				return false;
		} else if (!prevTerm.equals(other.prevTerm))
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String prev = "";
		if(prevTerm != null){
			prev = prevTerm.getKeyword().getWord();
		}
		
		String next = "";
		if(nextTerm != null){
			next = nextTerm.getKeyword().getWord();
		}
		return "Term [word=" + word + ", offset=" + offset + ", length=" + length + ", prevTerm=" + prev
				+ ", nextTerm=" + next + "]";
	}
	
}
