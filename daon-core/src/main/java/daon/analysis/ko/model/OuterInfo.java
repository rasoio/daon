package daon.analysis.ko.model;

/**
 * Created by mac on 2017. 3. 8..
 */
public class OuterInfo {

    //{"wordSeq":135975,"pOuterSeq":7098,"cnt":1}

    private int wordSeq;
    private int pOuterSeq;
    private long cnt;

    public int getWordSeq() {
        return wordSeq;
    }

    public void setWordSeq(int wordSeq) {
        this.wordSeq = wordSeq;
    }

    public int getpOuterSeq() {
        return pOuterSeq;
    }

    public void setpOuterSeq(int pOuterSeq) {
        this.pOuterSeq = pOuterSeq;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

    @Override
    public String toString() {
        return "OuterInfo{" +
                "wordSeq=" + wordSeq +
                ", pOuterSeq=" + pOuterSeq +
                ", cnt=" + cnt +
                '}';
    }
}
