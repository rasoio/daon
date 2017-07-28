package daon.analysis.ko;

import daon.analysis.ko.model.*;
import daon.analysis.ko.processor.CandidateFilterProcessor;
import daon.analysis.ko.processor.ConnectionProcessor;
import daon.analysis.ko.processor.DictionaryProcessor;
import daon.analysis.ko.processor.UnknownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DaonAnalyzer implements Serializable{

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

    private ModelInfo modelInfo;


    public DaonAnalyzer(ModelInfo modelInfo) throws IOException {

        this.modelInfo = modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public List<EojeolInfo> analyzeText(String text) throws IOException {

        List<EojeolInfo> eojeolInfos = new ArrayList<>();

        //공백 기준 분리
        String[] eojeols = text.split("\\s");

        //특수문자 분리

        //이전 어절 결과
        ResultInfo prevResultInfo = null;
        //다음 어절 결과
        ResultInfo nextResultInfo = null;

        //tokenizer results
        int length = eojeols.length;
        for(int i=0; i < length; i++){

            boolean isFirst = i == 0;
            boolean isLast = i == (length -1);

            String eojeol = eojeols[i];

            EojeolInfo info = new EojeolInfo();

            ResultInfo resultInfo = createResultInfo(eojeol);

            if(isFirst) {
                fillProcess(resultInfo);
            }else{
                resultInfo = nextResultInfo;
            }

            //다음 어절 구성
            if(isLast){
                nextResultInfo = null;
            }else{
                String nextEojeol = eojeols[i+1];
                nextResultInfo = createResultInfo(nextEojeol);
                fillProcess(nextResultInfo);
            }

            connectionProcess(prevResultInfo, resultInfo, nextResultInfo);

//            List<Term> terms = resultInfo.getTerms();
            List<Term> terms = resultInfo.getBestFilterSet().getTerms();

            prevResultInfo = resultInfo;

            info.setEojeol(eojeol);
            info.setTerms(terms);

            eojeolInfos.add(info);
        }

        return eojeolInfos;
    }

    private ResultInfo createResultInfo(String eojeol){
        char[] chars = eojeol.toCharArray();
        int length = chars.length;

        return ResultInfo.create(chars, length);
    }

    private void fillProcess(ResultInfo resultInfo) throws IOException {
        //사전 탐색 결과
        DictionaryProcessor.create(modelInfo).process(resultInfo);

        //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
        UnknownProcessor.create().process(resultInfo);

        CandidateFilterProcessor.create(modelInfo).process(resultInfo);
    }

    private void connectionProcess(ResultInfo beforeResult, ResultInfo resultInfo, ResultInfo nextResult) throws IOException {

        ConnectionProcessor.create(modelInfo).process(beforeResult, resultInfo, nextResult);

    }

}
