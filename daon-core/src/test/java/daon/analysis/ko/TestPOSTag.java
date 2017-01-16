package daon.analysis.ko;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.POSTag;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPOSTag {

    private Logger logger = LoggerFactory.getLogger(TestPOSTag.class);

    private int size = POSTag.fin.getIdx() + 1;

    private float connectionMatrix[][] = new float[size][size];

    @Test
    public void tags() {

        System.out.println(POSTag.na.getIdx());
        System.out.println(POSTag.ec.getIdx());
        System.out.println(POSTag.ex.getIdx());
        System.out.println((1l << 2));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                connectionMatrix[i][j] = Float.MAX_VALUE;
            }
        }


        float score = connectionMatrix[POSTag.fin.getIdx()][POSTag.fin.getIdx()];

        System.out.println(score);

    }
}
