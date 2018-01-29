package daon.core.handler;

import daon.core.config.POSTag;
import daon.core.data.Eojeol;
import daon.core.data.Morpheme;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.Node;
import daon.core.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MorphemeHandler extends AbstractHandler implements EojeolInfoHandler {

    private List<Morpheme> list = new ArrayList<>();

    @Override
    public void handle(EojeolInfo eojeolInfo) {

        for(Node node : eojeolInfo.getNodes()){
            Keyword[] keywords = node.getKeywords();
            for (int i = 0, len = keywords.length; i <len; i++) {
                Keyword keyword = keywords[i];

                if(isValid(keyword)) {
                    String word = keyword.getWord();
                    POSTag tag = keyword.getTag();
                    String type = tag.getName();
                    Morpheme morpheme = new Morpheme(i, word, type);
                    list.add(morpheme);
                }
            }
        }

    }

    @Override
    public List<Morpheme> getList() {
        return list;
    }

}
