package daon.analysis.ko;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TestCalculate {

    private Logger logger = LoggerFactory.getLogger(TestCalculate.class);

    private boolean test;

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException, InterruptedException {


        System.out.println(Math.log(10000));
        System.out.println(Math.log(1000));
        System.out.println(Math.log(100));
        System.out.println(Math.log(10));
        System.out.println(Math.log(1));
        System.out.println(Math.log(0.1));
        System.out.println(Math.log(0.01));
        System.out.println(Math.log(0.001));
        System.out.println(Math.log(0.0001));
        System.out.println(Math.log(0.0000000000000001));


        System.out.println(String.format("%.5f", sigmoid1Dx(-1000)));
        System.out.println(String.format("%.5f", sigmoid1Dx(-100)));
        System.out.println(String.format("%.5f", sigmoid1Dx(-10)));
        System.out.println(String.format("%.5f", sigmoid1Dx(-1)));
        System.out.println(String.format("%.5f", sigmoid1Dx(1)));
        System.out.println(String.format("%.5f", sigmoid1Dx(10)));
        System.out.println(String.format("%.5f", sigmoid1Dx(100)));
        System.out.println(String.format("%.5f", sigmoid1Dx(1000)));
        System.out.println(String.format("%.5f", sigmoid1Dx(10000)));


//        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (1)))));
//        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (10)))));
//        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (500)))));
//        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (-100)))));
//        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (1000)))));
//        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (-1000)))));





    }

    public static double sigmoid1Dx(final double value) {
        final double ex = Math.exp(value);
        return (2.0 * ex) / ((1 + ex) * (1 + ex));
    }
}
