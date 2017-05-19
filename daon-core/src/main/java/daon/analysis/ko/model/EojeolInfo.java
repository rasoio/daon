package daon.analysis.ko.model;

import java.util.List;

/**
 * 분석 결과
 */
public class EojeolInfo {

    private String eojeol;
    private List<Term> terms;

    public String getEojeol() {
        return eojeol;
    }

    public void setEojeol(String eojeol) {
        this.eojeol = eojeol;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    @Override
    public String toString() {

        return "{" +
                "eojeol='" + eojeol + '\'' +
                ", terms=" + terms +
                '}';
    }
}
