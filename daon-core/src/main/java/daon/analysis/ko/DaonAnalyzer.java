package daon.analysis.ko;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.util.CharTypeChecker;
import org.apache.lucene.analysis.util.RollingCharBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;

public class DaonAnalyzer {

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

    private Dictionary dictionary;

    public DaonAnalyzer(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public ResultTerms analyze(String text) throws IOException {
        //원본 문자
        char[] texts = text.toCharArray();

        //총 길이
        int textLength = text.length();

        ResultTerms results = dictionary.lookup(texts, 0, textLength);

        results.findBestPath();

        return results;
    }

    public List<Term> analyze(Reader input) throws IOException {

        List<Term> terms = new ArrayList<Term>();


        //포지션
        int pos = 0;
        int offset = 0;

        RollingCharBuffer buffer = new RollingCharBuffer();

        buffer.reset(input);

        while (pos > -1) {

            int length = 0;

            //공백 기준 분리
            while (true) {
                int ch = buffer.get(pos);

                if (ch == -1) {
                    pos = -1;
                    // End
                    break;
                }

                if (length == 0) {
                    offset = pos;
                }

                pos++;
                Config.CharType charType = CharTypeChecker.charType(ch);


//                logger.info("char : {}, type : {}, pos : {}, length : {}", (char) ch, charType, pos, length);

                if (Config.CharType.SPACE.equals(charType)) {
                    break;
                }

                length++;

                //            System.out.println(buffer.get(pos, 1));
                //            System.out.println((char) buffer.get(pos) +  " : " + charType);

            }


            //분리된 buffer 기준 분석
            if (length > 0) {
                char[] texts = buffer.get(offset, length);

//                logger.info("texts : {}, offset : {}, length : {}", texts, offset, length);

                //총 길이
                int textLength = length;

                ResultTerms results = dictionary.lookup(texts, 0, textLength);

                results.findBestPath();

//                logger.info("results : {}", results.getResults());
                terms.addAll(results.getResults());

//                buffer.freeBefore(pos);

            }

        }

        return terms;

    }

}
