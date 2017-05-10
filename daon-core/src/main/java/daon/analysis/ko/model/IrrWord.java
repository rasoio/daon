package daon.analysis.ko.model;

import java.util.List;

/**
 * 불규칙 정보
 */
public class IrrWord {

    //{"surface":"해도","wordSeqs":[189728,115317],"cnt":4217}

    /**
     * 표층형
     */
    private String surface;

    private int[] wordSeqs;

    private long cnt;

    public IrrWord() {}

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public int[] getWordSeqs() {
        return wordSeqs;
    }

    public void setWordSeqs(int[] wordSeqs) {
        this.wordSeqs = wordSeqs;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

    @Override
    public String toString() {
        return "IrrWord{" +
                "surface='" + surface + '\'' +
                ", wordSeqs=" + wordSeqs +
                ", cnt=" + cnt +
                '}';
    }
}
