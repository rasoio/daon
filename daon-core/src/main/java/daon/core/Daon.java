package daon.core;

import daon.core.config.POSTag;
import daon.core.result.*;
import daon.core.processor.ConnectionProcessor;
import daon.core.processor.DictionaryProcessor;
import daon.core.util.ModelUtils;
import daon.core.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Daon {

    public Daon() {}

    public List<EojeolInfo> analyze(String sentence) throws IOException {

        if(sentence == null || sentence.length() == 0){
            return new ArrayList<>();
        }

        char[] chars = sentence.toCharArray();
        int length = chars.length;

        return analyze(chars, length);
    }

    public List<Keyword> pos(String sentence) throws IOException {
        List<Keyword> keywords = new ArrayList<>();

        List<EojeolInfo> eojeols = analyze(sentence);

        eojeols.forEach((EojeolInfo e) -> {
            e.getNodes().forEach((Node t) -> {
                keywords.addAll(Arrays.asList(t.getKeywords()));
            });
        });

        return keywords;
    }

    public List<String> nouns(String sentence) throws IOException {
        List<String> nouns = pos(sentence).stream().filter(keyword -> (Utils.containsTag(POSTag.NOUNS, keyword.getTag()))).map(Keyword::getWord).collect(Collectors.toList());

        return nouns;
    }

    public List<EojeolInfo> analyze(char[] chars, int length) throws IOException {

        if(chars == null || length == 0){
            return new ArrayList<>();
        }

        ModelInfo modelInfo = ModelUtils.getModel();

        Lattice lattice = new Lattice(chars, length);

        DictionaryProcessor.create(modelInfo).process(lattice);

        ConnectionProcessor.create(modelInfo).process(lattice);

        return lattice.getEojeolInfos();
    }

}
