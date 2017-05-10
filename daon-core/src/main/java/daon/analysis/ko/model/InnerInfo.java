package daon.analysis.ko.model;

/**
 * Created by mac on 2017. 3. 8..
 */
public class InnerInfo {

    //{"wordSeq":46498,"pInnerSeq":144094,"nInnerSeq":136088,"cnt":87}
    private int wordSeq;
    private int pInnerSeq;
    private int nInnerSeq;
    private long cnt;

    public int getWordSeq() {
        return wordSeq;
    }

    public void setWordSeq(int wordSeq) {
        this.wordSeq = wordSeq;
    }

    public int getpInnerSeq() {
        return pInnerSeq;
    }

    public void setpInnerSeq(int pInnerSeq) {
        this.pInnerSeq = pInnerSeq;
    }

    public int getnInnerSeq() {
        return nInnerSeq;
    }

    public void setnInnerSeq(int nInnerSeq) {
        this.nInnerSeq = nInnerSeq;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

    @Override
    public String toString() {
        return "InnerInfo{" +
                "wordSeq=" + wordSeq +
                ", pInnerSeq=" + pInnerSeq +
                ", nInnerSeq=" + nInnerSeq +
                ", cnt=" + cnt +
                '}';
    }
}
