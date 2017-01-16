package daon.analysis.ko;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import daon.analysis.ko.dict.connect.ConnectionCosts;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.dict.connect.ConnectionCostsBuilder;

public class TestStep1 {

    private Logger logger = LoggerFactory.getLogger(TestStep1.class);

    private static String encoding = Charset.defaultCharset().name();

    private static Dictionary dic;
    private static ConnectionCosts connectionCosts;

    private static List<String> keywords;

    public static void loadTestCase(String fileName) throws Exception {

        final InputStream in = TestStep1.class.getResourceAsStream(fileName);

        try {
            keywords = IOUtils.readLines(in, Charsets.toCharset(encoding));
        } finally {
            IOUtils.closeQuietly(in);
        }

    }

    @BeforeClass
    public static void load() throws Exception {

        //테스트 케이스 파일 로딩
        loadTestCase("step1.txt");

        //기분석 사전 로딩
        loadDictionary();
    }

    private static void loadDictionary() throws Exception {
        connectionCosts = ConnectionCostsBuilder.create()
                .setFileName("connect_matrix.dic")
                .setReader(new FileReader<>())
                .setValueType(TagConnection.class).build();
        dic = DictionaryBuilder.create()
                .setFileName("rouzenta_trans.dic")
                .setReader(new FileReader<>())
                .setValueType(Keyword.class)
                .setConnectionCosts(connectionCosts).build();
    }

    @Test
    public void analyzeStep1Test() throws IOException, InterruptedException {

        for (String text : keywords) {

            if (text.startsWith("#")) {
                continue;
            }

            DaonAnalyzer analyzer = new DaonAnalyzer(dic);

            ResultTerms results = analyzer.analyze(text);

            logger.info("################ results #################");
            logger.info("text : {}", text);
            for (Term t : results.getResults()) {
                logger.info("term : {}", t);
            }

        }

//		Thread.sleep(1000000);
    }
}
