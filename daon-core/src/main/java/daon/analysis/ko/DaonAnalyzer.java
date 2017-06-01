package daon.analysis.ko;

import daon.analysis.ko.model.*;
import daon.analysis.ko.processor.ConnectionProcessor;
import daon.analysis.ko.processor.DictionaryProcessor;
import daon.analysis.ko.processor.UnknownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class DaonAnalyzer implements Serializable{

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

    private ModelInfo modelInfo;

    public DaonAnalyzer(ModelInfo modelInfo) throws IOException {

        this.modelInfo = modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public List<Term> analyze(String text) throws IOException {

        List<Term> terms = new ArrayList<>();

        String[] eojeols = text.split("\\s");

        Keyword outerPrev = null;

        //tokenizer results
        for(String eojeol : eojeols) {

            outerPrev = process(terms, outerPrev, eojeol);

        }

        return terms;
    }


    public List<EojeolInfo> analyzeText(String text) throws IOException {

        List<EojeolInfo> eojeolInfos = new ArrayList<>();

        String[] eojeols = text.split("\\s");

        Keyword outerPrev = null;

        //tokenizer results
        for(String eojeol : eojeols) {

            EojeolInfo info = new EojeolInfo();

            List<Term> terms = new ArrayList<>();

            outerPrev = process(terms, outerPrev, eojeol);

            info.setEojeol(eojeol);
            info.setTerms(terms);

            eojeolInfos.add(info);

        }

        return eojeolInfos;
    }

    private Keyword process(List<Term> terms, Keyword outerPrev, String eojeol) throws IOException {

        char[] chars = eojeol.toCharArray();
        int length = chars.length;

        ResultInfo resultInfo = ResultInfo.create(chars, length);

        //사전 탐색 결과
        DictionaryProcessor.create(modelInfo).process(resultInfo);

        //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
        UnknownProcessor.create().process(resultInfo);

        //connection 찾기
        ConnectionProcessor.create(modelInfo).process(outerPrev, resultInfo);

        Term lastTerm = resultInfo.getLastTerm();

        if(lastTerm != null) {
            outerPrev = lastTerm.getLast();
        }

        terms.addAll(resultInfo.getTerms());

        return outerPrev;
    }

}
