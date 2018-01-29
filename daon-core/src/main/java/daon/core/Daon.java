package daon.core;

import daon.core.config.POSTag;
import daon.core.data.Eojeol;
import daon.core.data.Morpheme;
import daon.core.handler.DefaultHandler;
import daon.core.handler.EojeolInfoHandler;
import daon.core.handler.MorphemeHandler;
import daon.core.result.*;
import daon.core.processor.ConnectionProcessor;
import daon.core.processor.DictionaryProcessor;
import daon.core.util.ModelUtils;
import daon.core.util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Daon {

    public Daon() {}

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

    public List<Morpheme> morphemes(String sentence) throws IOException {

        return morphemes(sentence, null, null);
    }

    public List<Morpheme> morphemes(String sentence, String include, String exclude) throws IOException {

        MorphemeHandler handler = new MorphemeHandler();

        if(include != null && !include.isEmpty()){
            String[] includeTags = include.split("[,]");
            long includeBit = Utils.makeTagBit(includeTags);
            handler.setIncludeBit(includeBit);
        }
        if(exclude != null && !exclude.isEmpty()){
            String[] excludeTags = exclude.split("[,]");
            long excludeBit = Utils.makeTagBit(excludeTags);
            handler.setExcludeBit(excludeBit);
        }

        analyzeWithHandler(sentence, handler);

        return handler.getList();
    }

}