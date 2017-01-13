package daon.analysis.ko.model;

import daon.analysis.ko.dict.config.Config.POSTag;

public class TagCost {
	private POSTag tag;
	
	private long cnt;
	
	private float prob;

	public TagCost() {}

	public TagCost(String tag, long cnt, float prob) {
		super();
		this.tag = POSTag.valueOf(tag);
		this.cnt = cnt;
		this.prob = prob;
	}

	public POSTag getTag() {
		return tag;
	}

	public void setTag(POSTag tag) {
		this.tag = tag;
	}

	public long getCnt() {
		return cnt;
	}

	public void setCnt(long cnt) {
		this.cnt = cnt;
	}

	public float getProb() {
		return prob;
	}

	public void setProb(float prob) {
		this.prob = prob;
	}

	@Override
	public String toString() {
		return "(tag=" + tag + ", cnt=" + cnt + ", prob=" + prob + ")";
	}
}
