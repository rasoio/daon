package daon.core;

import daon.core.data.Eojeol;
import daon.core.data.Morpheme;
import daon.core.handler.DefaultHandler;
import daon.core.handler.EojeolInfoHandler;
import daon.core.handler.MorphemeHandler;
import daon.core.processor.ConnectionProcessor;
import daon.core.processor.DictionaryProcessor;
import daon.core.result.Lattice;
import daon.core.result.ModelInfo;
import daon.core.util.ModelUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Daon {

    public Daon() {
        // init model
        ModelInfo currentModel = ModelUtils.getModel();
        if(currentModel == null){
            ModelUtils.init();
        }
    }

    public List<Eojeol> analyze(String sentence) throws IOException {

        DefaultHandler handler = new DefaultHandler();

        analyzeWithHandler(sentence, handler);

        return handler.getList();
    }

    public List<Eojeol> analyze(char[] chars, int length) throws IOException {

        DefaultHandler handler = new DefaultHandler();

        analyzeWithHandler(chars, length, handler);

        return handler.getList();
    }

    public void analyzeWithHandler(String sentence, EojeolInfoHandler handler) throws IOException {

        if(sentence == null || sentence.length() == 0){
            return;
        }

        char[] chars = sentence.toCharArray();
        int length = chars.length;

        analyzeWithHandler(chars, length, handler);
    }

    public void analyzeWithHandler(char[] chars, int length, EojeolInfoHandler handler) throws IOException {

        if(chars == null || length == 0){
            return;
        }

        ModelInfo modelInfo = ModelUtils.getModel();

        Lattice lattice = new Lattice(chars, length);

        DictionaryProcessor.create(modelInfo).process(lattice);

        ConnectionProcessor.create(modelInfo).process(lattice, handler);
    }

    /**
     * for python
     * @param sentence 분석 대상 문장
     * @return 분석결과
     * @throws IOException 분석 예외
     */
    public String morphemes(String sentence) throws IOException {

        return morphemes(sentence, -1, -1);
    }

    /**
     * for python
     * @param sentence 분석 대상 문장
     * @param includeBit 포함 할 tag bit
     * @param excludeBit 제외 할 tag bit
     * @return 분석결과
     * @throws IOException 분석 예외
     */
    public String morphemes(String sentence, long includeBit, long excludeBit) throws IOException {

        MorphemeHandler handler = new MorphemeHandler();
        handler.setIncludeBit(includeBit);
        handler.setExcludeBit(excludeBit);
        analyzeWithHandler(sentence, handler);

        List<Morpheme> list = handler.getList();

        return makeString(list);
    }

    private String makeString(List<Morpheme> list){
        if(list.size() == 0){
            return "";
        }

        Iterator<Morpheme> it = list.iterator();
        StringBuilder sb = new StringBuilder();
        for (;;) {
            Morpheme e = it.next();
            sb.append(e.getWord()).append("/").append(e.getTag());
            if (! it.hasNext())
                return sb.toString();
            sb.append(' ');
        }
    }

}