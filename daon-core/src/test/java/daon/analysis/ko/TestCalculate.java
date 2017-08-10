package daon.analysis.ko;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestCalculate {

    private Logger logger = LoggerFactory.getLogger(TestCalculate.class);

    private boolean test;

    @Test
    public void test() throws IOException, InterruptedException {


        System.out.println(-Math.log(10000));
        System.out.println(-Math.log(1000));
        System.out.println(-Math.log(100));
        System.out.println(-Math.log(10));
        System.out.println(-Math.log(1));
        System.out.println(-Math.log(2));
        System.out.println(-Math.log(3));
        System.out.println(-Math.log(0.1));
        System.out.println(-Math.log(0.01));
        System.out.println(-Math.log(0.001));
        System.out.println(-Math.log(0.0001));
        System.out.println(-Math.log(0.0000000000000001));


        System.out.println(Math.tan(1));
        System.out.println(Math.tan(2));
        System.out.println(Math.tan(3));

        System.out.println(String.format("%.5f", sigmoid1Dx(-1000)));
        System.out.println(String.format("%.5f", sigmoid1Dx(-100)));
        System.out.println(String.format("%.5f", sigmoid1Dx(-10)));
        System.out.println(String.format("%.5f", sigmoid1Dx(-1)));
        System.out.println(String.format("%.5f", sigmoid1Dx(1)));
        System.out.println(String.format("%.5f", sigmoid1Dx(10)));
        System.out.println(String.format("%.5f", sigmoid1Dx(100)));
        System.out.println(String.format("%.5f", sigmoid1Dx(1000)));
        System.out.println(String.format("%.5f", sigmoid1Dx(10000)));


        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (1)))));
        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (10)))));
        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (500)))));
        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (-100)))));
        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (1000)))));
        System.out.println(String.format("%.5f", 1 / (1 + Math.exp(-1 * (-1000)))));


        System.out.println(String.format("%.20f", sigmoid(100000)));
        System.out.println(String.format("%.20f", sigmoid(100)));
        System.out.println(String.format("%.20f", sigmoid(10)));
        System.out.println(String.format("%.20f", sigmoid(1)));
        System.out.println(String.format("%.20f", sigmoid(0.9999999999)));
        System.out.println(String.format("%.20f", sigmoid(0.0999999999)));
        System.out.println(String.format("%.20f", sigmoid(0.0099999999)));
        System.out.println(String.format("%.20f", sigmoid(0.0009999999)));
        System.out.println(String.format("%.20f", sigmoid(0.0004714913)));
        System.out.println(String.format("%.20f", sigmoid(0.0000089201)));
        System.out.println(String.format("%.20f", sigmoid(0.0000089202)));




        System.out.println(String.format("%.5f", Math.exp(1)));
        System.out.println(String.format("%.5f", Math.exp(10)));
        System.out.println(String.format("%.5f", Math.exp(100)));
        System.out.println(String.format("%.5f", Math.exp(1000)));
        System.out.println(String.format("%.5f", Math.exp(10000)));

    }

    public static double sigmoid1Dx(final double value) {
        final double ex = Math.exp(value);
        return (2.0 * ex) / ((1 + ex) * (1 + ex));
    }

    public static double sigmoid(double x) {
        return (1/( 1 + Math.pow(Math.E,(-1*x))));
    }

    @Test
    public void cal2(){

        System.out.println(Math.log(1000000));
        System.out.println(Math.log(1000000));
        System.out.println(Math.log(1000000));
//        System.out.println(toCost(Math.log(1000000), 2));
//        System.out.println(Math.log(100000));
//        System.out.println(Math.log(10000));
//        System.out.println(toCost(Math.log(1000), 2));
//        System.out.println(Math.log(100));
//        System.out.println(Math.log(10));
//        System.out.println(toCost(Math.log(1), 2));
//        System.out.println(toCost(Math.tan(1000000), 200));
//        System.out.println(toCost(Math.tan(100000), 200));
//        System.out.println(toCost(Math.log(10000), 200));
//        System.out.println(toCost(Math.log(1000), 200));
//        System.out.println(toCost(Math.log(100), 200));
//        System.out.println(toCost(Math.tan(10), 200));
//        System.out.println(toCost(Math.tan(1), 200));



        System.out.println(toCost(Math.log(1), 200));
        System.out.println(toCost(Math.log(0.998), 200));
        System.out.println(toCost(Math.log(0.098), 200));
        System.out.println(toCost(Math.log(0.009), 200));
        System.out.println(toCost(Math.log(0.000001), 200));
        System.out.println(toCost(Math.log(0.00000000000001), 200));

//        System.out.println(toCost(2.12d, 800));

    }

    private int toCost(double d, int n){
        short max = +32767;
        short min = -32767;

//        double score = Math.log(d);

        return (short)(-n * d);
    }
}
