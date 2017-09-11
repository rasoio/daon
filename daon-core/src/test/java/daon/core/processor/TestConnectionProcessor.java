package daon.core.processor;

import daon.core.model.Unknown;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

//import static daon.analysis.ko.processor.ConnectionProcessor.scoreComparator;

/**
 * Created by mac on 2017. 5. 19..
 */
public class TestConnectionProcessor {

    private Logger logger = LoggerFactory.getLogger(TestConnectionProcessor.class);


    @Test
    public void test1() throws IOException, InterruptedException {
//        TreeSet<CandidateSet> candidateSets = new TreeSet<>(scoreComparator);

//        CandidateSet candidateSet = new CandidateSet();
//        candidateSets.add(candidateSet);

//        CandidateSet first = candidateSets.first();

        String test = "     일이삼  사오 육칠팔.~= . . . ";
        char[] texts = test.toCharArray();

//        WhitespaceDelimiter whitespaceDelimiter = WhitespaceDelimiter.create(texts);
        WhitespaceDelimiter whitespaceDelimiter = new WhitespaceDelimiter(texts);

        while (whitespaceDelimiter.next() != WhitespaceDelimiter.DONE){
            int offset = whitespaceDelimiter.current;
            int length = whitespaceDelimiter.end - whitespaceDelimiter.current;

            String word = new String(texts, offset, length);


            logger.info("'" + word + "' (" + offset + "," + length + ")");
        }
    }

    @Test
    public void testUnknown(){

        Unknown unknown = new Unknown();

        unknown.add(0);
        unknown.add(1);
        unknown.add(2);
        unknown.add(3);
        unknown.add(4);
        unknown.add(5);
        unknown.add(9);
        unknown.add(10);
        unknown.add(11);
        unknown.add(13);

        List<Unknown.Position> positions = unknown.getList();

        for (Unknown.Position position : positions){
            logger.info("offset : {}, length : {}", position.getOffset(), position.getLength());
        }
    }
}
