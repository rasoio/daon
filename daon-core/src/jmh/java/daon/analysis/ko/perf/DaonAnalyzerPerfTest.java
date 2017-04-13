package daon.analysis.ko.perf;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.model.ResultTerms;
import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@State(Scope.Benchmark)
public class DaonAnalyzerPerfTest {

    private static List<String> keywords = new ArrayList<String>();

    private static Dictionary dictionary;
//    private static ConnectionCosts connectionCosts;

    @Setup
    public void setup() throws IOException {

        File seojong = new File("/Users/mac/Downloads/sejong.txt");

        String seojongTxt = FileUtils.readFileToString(seojong, Charset.defaultCharset());

//        keywords.add(seojongTxt.substring(0, 100000));
        keywords.add("8.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)");

        try {

//            connectionCosts = ConnectionCostsBuilder.create()
//                    .setFileName("connect_matrix.dic")
//                    .setReader(new JsonFileReader<TagConnection>())
//                    .setValueType(TagConnection.class).build();
//            dictionary = DictionaryBuilder.create()
//                    .setFileName("rouzenta_trans.dic")
//                    .setReader(new JsonFileReader<Keyword>())
//                    .setValueType(Keyword.class)
//                    .setConnectionCosts(connectionCosts).build();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    @Benchmark
    public void daonAnalyzerReader() {

        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(dictionary);

        try {

            for (String source : keywords) {

//                ResultTerms terms = daonAnalyzer.analyze(source);
                StringReader input = new StringReader(source);
                daonAnalyzer.analyze(input);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//			daonAnalyzer.close();
        }
    }

//    @Benchmark
    public void daonAnalyzerString() {

        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(dictionary);

        try {

            for (String source : keywords) {

                ResultTerms terms = daonAnalyzer.analyze(source);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//			daonAnalyzer.close();
        }
    }

}
