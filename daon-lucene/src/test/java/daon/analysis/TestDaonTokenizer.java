package daon.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestDaonTokenizer extends BaseTokenStreamTestCase {

    private Logger logger = LoggerFactory.getLogger(TestDaonTokenizer.class);

    private Analyzer analyzer;
//    private String input = "하루아침에 되나?";
    private String input = "우리나라에서 만세 " + line() + " ee " + line();

    @Before
    public void before() throws IOException {
        analyzer = new DaonAnalyzer();

//        input = getStringFromTestCase();
    }

    private String getStringFromTestCase() throws IOException {
        InputStream input = this.getClass().getResourceAsStream("testcase.txt");

        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line);
                textBuilder.append(System.lineSeparator());
            }
        }

        return textBuilder.toString();
    }

    public void testLongText() throws IOException {

        TokenStream ts = analyzer.tokenStream("bogus", input);
        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAtt = ts.addAttribute(TypeAttribute.class);
        PositionIncrementAttribute posIncAtt = ts.addAttribute(PositionIncrementAttribute.class);

        ts.reset();
        while (ts.incrementToken()) {
            logger.info("term : {}, ({},{}), type : {}, posInc : {}", termAtt.toString(), offsetAtt.startOffset(), offsetAtt.endOffset(), typeAtt.type(), posIncAtt.getPositionIncrement());
        }
        ts.end();
        ts.close();
    }

    public void testAnalyze() throws IOException {
        String input = "우리나라 만세 " + line() + " ee " + line();

        assertAnalyzesTo(analyzer, input,
                new String[] { "우리나라", "만세", "ee" },
                new int[] { 0, 5, 11},
                new int[] { 4, 7, 13}
        );
    }

    public String line(){
//        return System.lineSeparator();
        return "\r\n";
    }
}
