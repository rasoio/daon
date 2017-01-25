package daon.dictionary.model;

import java.util.List;

/**
 * Created by mac on 2017. 1. 24..
 */
public class Sentence {

    private String sentence;

    private List<Eojeol> eojeols;

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public List<Eojeol> getEojeols() {
        return eojeols;
    }

    public void setEojeols(List<Eojeol> eojeols) {
        this.eojeols = eojeols;
    }
}
