package daon.core.data;

import java.util.ArrayList;
import java.util.List;

public class Word {
    private String surface;
    private List<Morpheme> morphemes = new ArrayList<>();
    private int weight;

    public Word() {}

    public Word(String surface, List<Morpheme> morphemes) {
        this.surface = surface;
        this.morphemes = morphemes;
    }

    public Word(String surface, List<Morpheme> morphemes, int weight) {
        this.surface = surface;
        this.morphemes = morphemes;
        this.weight = weight;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
