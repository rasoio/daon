package daon.analysis.ko.perf;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.connect.ConnectMatrix;
import daon.analysis.ko.dict.connect.ConnectMatrixBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.TagConnection;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.ArrayList;
import java.util.List;

@State(Scope.Benchmark)
public class DaonAnalyzerPerfTest {

    private static List<String> keywords = new ArrayList<String>();

    private static Dictionary dictionary;
    private static ConnectMatrix connectMatrix;

    @Setup
    public void setup(){

        keywords.add("8.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)");

        try {

            connectMatrix = ConnectMatrixBuilder.create()
                    .setFileName("connect_matrix.dic")
                    .setReader(new FileReader<TagConnection>())
                    .setValueType(TagConnection.class).build();
            dictionary = DictionaryBuilder.create()
                    .setFileName("rouzenta_trans.dic")
                    .setReader(new FileReader<Keyword>())
                    .setValueType(Keyword.class)
                    .setConnectMatrix(connectMatrix).build();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Benchmark
    public void daonAnalyzer() {

        DaonAnalyzer daonAnalyzer = new DaonAnalyzer(dictionary);

        try{

            for(String source : keywords) {

                ResultTerms terms = daonAnalyzer.analyze(source);
            }

        }catch(Exception e){
            e.printStackTrace();
        } finally {
//			daonAnalyzer.close();
        }
    }

}
