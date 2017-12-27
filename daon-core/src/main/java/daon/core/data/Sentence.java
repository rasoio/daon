package daon.core.data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sentence implements Serializable {

    private String sentence;
    private List<Eojeol> eojeols = new ArrayList<>();

    public Sentence() {
    }

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
