package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by mac on 2017. 5. 26..
 */
public class TestResultInfo {

    @Test
    public void testCheckBits(){

        ResultInfo resultInfo = ResultInfo.create("".toCharArray(), 10);


        resultInfo.addCandidateTerm(getDummyTerm(0,3, "123"));
        resultInfo.addCandidateTerm(getDummyTerm(5,3, "123"));

        boolean[] check = resultInfo.getCheckBits();

        assertArrayEquals(new boolean[]{true, true, true, false, false, true, true, true, false, false}, check);
    }

    @Test
    public void testMissRanges(){

        ResultInfo resultInfo = ResultInfo.create("".toCharArray(), 10);

        resultInfo.addCandidateTerm(getDummyTerm(1,3, "123"));
        resultInfo.addCandidateTerm(getDummyTerm(5,3, "123"));
        resultInfo.addCandidateTerm(getDummyTerm(9,1, "1"));


        boolean[] check = resultInfo.getCheckBits();

        System.out.println(ToStringBuilder.reflectionToString(check));
        List<ResultInfo.MissRange> missRanges = resultInfo.getMissRange();


        System.out.println("" + missRanges);
    }

    private Term getDummyTerm(int offset, int length, String word){
        return new Term(offset,length, word, ExplainInfo.create().unknownMatch(), 0, new Keyword(word, POSTag.NNG));
    }
}
