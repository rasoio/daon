package daon.analysis.ko.main;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.connect.ConnectionCosts;
import daon.analysis.ko.dict.connect.ConnectionCostsBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.Term;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by mac on 2017. 1. 5..
 */
public class DaonAnalyzerExample {

    public static void main(String[] args) throws Exception {

        ConnectionCosts connectionCosts = ConnectionCostsBuilder.create()
                .setFileName("connect_matrix.dic")
                .setReader(new FileReader<TagConnection>())
                .setValueType(TagConnection.class).build();
        Dictionary dic = DictionaryBuilder.create()
                .setFileName("rouzenta_trans.dic")
                .setReader(new FileReader<Keyword>())
                .setValueType(Keyword.class)
                .setConnectionCosts(connectionCosts).build();


        File seojong = new File("/Users/mac/Downloads/sejong.txt");

        String seojongTxt = FileUtils.readFileToString(seojong, Charset.defaultCharset());
        StringReader input = new StringReader(seojongTxt);
//        StringReader input = new StringReader("€123 나이키\uD801\uDC37\uD852\uDF62");
//        StringReader input = new StringReader("123 나이키 123ABCMART");

        DaonAnalyzer analyzer = new DaonAnalyzer(dic);

        StopWatch watch = new StopWatch();
        watch.start();
//        ResultTerms results = analyzer.analyze("아버지가방에들어가신다");
        List<Term> terms = analyzer.analyze(input);

        int lineCnt = 0;
        System.out.println("################ results #################");
        for(Term t : terms){
//            if(lineCnt > 21547360) {
//                System.out.println(t);
//            }
            lineCnt++;
        }

        watch.stop();

        System.out.println(watch.getTime());
        System.out.println(lineCnt);
    }
}
