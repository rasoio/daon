package daon.analysis.ko.model;


/**
 * Created by mac on 2017. 3. 8..
 */
public class TagTran {

    //{"wordSeq":46498,"pInnerSeq":144094,"nInnerSeq":136088,"cnt":87}
    private String tag;
    private String nInnerTag;
    private long cnt;


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getnInnerTag() {
        return nInnerTag;
    }

    public void setnInnerTag(String nInnerTag) {
        this.nInnerTag = nInnerTag;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

    @Override
    public String toString() {
        return "TagTran{" +
                "tag=" + tag +
                ", nInnerTag=" + nInnerTag +
                ", cnt=" + cnt +
                '}';
    }
}
