package daon.analysis.ko.main;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Created by mac on 2017. 1. 5..
 */
public class DaonAnalyzerExample {

    public static void main(String[] args) throws Exception {

        File seojong = new File("/Users/mac/Downloads/sejong.txt");

        String seojongTxt = FileUtils.readFileToString(seojong, Charset.defaultCharset());
        StringReader input = new StringReader(seojongTxt);
//        StringReader input = new StringReader("€123 나이키\uD801\uDC37\uD852\uDF62");
//        StringReader input = new StringReader("123 나이키 123ABCMART");

//        DaonAnalyzer analyzer = new DaonAnalyzer(dic);

        StopWatch watch = new StopWatch();
        watch.start();
//        ResultTerms results = analyzer.analyze("아버지가방에들어가신다");
//        List<Term> terms = analyzer.analyze(input);

        int lineCnt = 0;
        System.out.println("################ results #################");
//        for (Term t : terms) {
//            if(lineCnt > 21547360) {
//                System.out.println(t);
//            }
//            lineCnt++;
//        }

        watch.stop();

        System.out.println(watch.getTime());
        System.out.println(lineCnt);
    }
}
