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


    public List<Term> analyze(String text) throws IOException {

        List<Term> terms = new ArrayList<>();

        String[] eojeols = text.split("\\s");

        Keyword outerPrev = null;

        //tokenizer results
        for(String eojeol : eojeols) {

            char[] chars = eojeol.toCharArray();
            int length = chars.length;

            //사전 탐색 결과
            TreeMap<Integer, List<Term>> dictionaryResults = DictionaryProcessor.create(modelInfo).process(chars, length);

            //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
            TreeMap<Integer, Term> results = UnknownProcessor.create().process(chars, length, dictionaryResults);

            //connection 찾기
            results = ConnectionProcessor.create(modelInfo).process(outerPrev, length, dictionaryResults, results);

            results.entrySet().forEach(e->{
                terms.add(e.getValue());
            });

            Map.Entry<Integer, Term> e = results.lastEntry();

            //last word seq
            if(e != null) {
                outerPrev = e.getValue().getLast();
            }

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

            char[] chars = eojeol.toCharArray();
            int length = chars.length;

            //사전 탐색 결과
            TreeMap<Integer, List<Term>> dictionaryResults = DictionaryProcessor.create(modelInfo).process(chars, length);

            //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
            TreeMap<Integer, Term> results = UnknownProcessor.create().process(chars, length, dictionaryResults);

            //connection 찾기
            results = ConnectionProcessor.create(modelInfo).process(outerPrev, length, dictionaryResults, results);

            results.entrySet().forEach(e->{
                terms.add(e.getValue());
            });

            Map.Entry<Integer, Term> e = results.lastEntry();

            //last word seq
            if(e != null) {
                outerPrev = e.getValue().getLast();
            }

            info.setEojeol(eojeol);
            info.setTerms(terms);

            eojeolInfos.add(info);

        }

        return eojeolInfos;
    }





}
