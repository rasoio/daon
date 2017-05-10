package daon.analysis.ko.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 분석 결과
 */
public class EojeolInfo {

    private String eojeol;
    private List<CandidateTerm> terms;

    public String getEojeol() {
        return eojeol;
    }

    public void setEojeol(String eojeol) {
        this.eojeol = eojeol;
    }

    public List<CandidateTerm> getTerms() {
        return terms;
    }

    public void setTerms(List<CandidateTerm> terms) {
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
