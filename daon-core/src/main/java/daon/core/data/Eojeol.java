package daon.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Eojeol implements Serializable {
    private int seq;
    private String surface;
    private List<Morpheme> morphemes = new ArrayList<>();

    public Eojeol() {
    }

    public Eojeol(int seq, String surface) {
        this.seq = seq;
        this.surface = surface;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public List<Morpheme> getMorphemes() {
        return morphemes;
    }

    public void setMorphemes(List<Morpheme> morphemes) {
        this.morphemes = morphemes;
    }
}
