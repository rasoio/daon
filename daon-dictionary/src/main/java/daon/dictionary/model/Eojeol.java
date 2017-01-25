package daon.dictionary.model;

import java.util.List;

/**
 * Created by mac on 2017. 1. 24..
 */
public class Eojeol {

    private String surface;

    private List<Morpheme> morphemes;


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
