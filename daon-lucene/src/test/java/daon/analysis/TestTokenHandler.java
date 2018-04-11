package daon.analysis;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import daon.core.config.MatchType;
import daon.core.config.POSTag;
import daon.core.data.Morpheme;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.Node;
import daon.core.util.Utils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestTokenHandler {

    private Logger logger = LoggerFactory.getLogger(TestTokenHandler.class);

    @Test
    public void testTokenHandler() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("testcase.csv");


        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(settings);
        List<String[]> rows = parser.parseAll(input);
        for (String[] record : rows) {
            String surface = record[0];
            String morphemes = record[1];
            String answer = record[2];

            List<Morpheme> morphs = Utils.parseMorpheme(morphemes);

            Keyword[] keywords = morphs.stream().map(m->new Keyword(m.getWord(), POSTag.valueOf(m.getTag()))).toArray(Keyword[]::new);

            EojeolInfo info = new EojeolInfo();
            info.setSeq(0);
            info.setOffset(0);
            info.setSurface(surface);

            Node node = new Node(0, surface.length(), surface, 1, MatchType.WORDS, keywords);

            info.addNode(node);

            TokenHandler tokenHandler = new TokenHandler();
            tokenHandler.handle(info);
            List<Token> tokens = tokenHandler.getList();

            String keywordsStr = Stream.of(keywords).map(k->k.getWord() + "/" + k.getTag()).collect(Collectors.joining(" "));

            String result = tokens.stream().map(t->t.getTerm() + "/" + t.getType() + "(" + t.getStartOffset() + ":" + t.getEndOffset()+ ")")
                    .collect(Collectors.joining(" "));
            logger.info("surface : {} || keywords : {} || result : {}", surface, keywordsStr, result);
        }

    }


}
