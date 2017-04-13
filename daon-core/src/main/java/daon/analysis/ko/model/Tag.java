package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;

/**
 * Created by mac on 2017. 3. 8..
 */
public class Tag {

    //{"wordSeq":46498,"pInnerSeq":144094,"nInnerSeq":136088,"cnt":87}
    private String tag;
    private long cnt;


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tag=" + tag +
                ", cnt=" + cnt +
                '}';
    }
}
