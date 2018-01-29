package daon.core.handler;

import daon.core.config.POSTag;
import daon.core.data.Eojeol;
import daon.core.data.Morpheme;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.Node;

import java.util.ArrayList;
import java.util.List;

public class DefaultHandler implements EojeolInfoHandler {

    private List<Eojeol> list = new ArrayList<>();

    @Override
    public void handle(EojeolInfo eojeolInfo) {

        Eojeol eojeol = new Eojeol(eojeolInfo.getSeq(), eojeolInfo.getSurface());

        for(Node node : eojeolInfo.getNodes()){
            Keyword[] keywords = node.getKeywords();
            for (int i = 0, len = keywords.length; i <len; i++) {
                Keyword keyword = keywords[i];
                String word = keyword.getWord();
                POSTag tag = keyword.getTag();
                String type = tag.getName();

                Morpheme morpheme = new Morpheme(i, word, type);

                eojeol.addMorpheme(morpheme);
            }
        }

        list.add(eojeol);
    }

    @Override
    public List<Eojeol> getList() {
        return list;
    }

}
