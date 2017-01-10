package daon.analysis.ko.perf;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.connect.ConnectMatrix;
import daon.analysis.ko.dict.connect.ConnectMatrixBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.Term;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class ConnectionMatrixPerfTest {

    private static Map<String,Float> results = new HashMap<String,Float>();
    private int size = Config.POSTag.fin.getIdx() + 1;

    private float connectionMatrix[][] = new float[size][size];

    @Setup
    public void setup(){

        results.put("na|ps",10f);

        for(int i=0; i< size;i++){
            for(int j=0; j< size;j++){
                connectionMatrix[i][j] = Float.MAX_VALUE;
            }
        }


    }

//    @Benchmark
    public void get(Blackhole bh) {

        IntStream.range(0,20).forEach(i -> {

            float score = results.get("na|ps");

            bh.consume(score);
        });
    }

//    @Benchmark
    public void getArray(Blackhole bh) {

        IntStream.range(0,20).forEach(i -> {

            float score = connectionMatrix[Config.POSTag.fin.getIdx()][Config.POSTag.fin.getIdx()];

            bh.consume(score);
        });
    }

    @Benchmark
    public void equals(Blackhole bh){

        boolean check = Config.POSTag.cp.equals(Config.POSTag.cp);

        bh.consume(check);
    }

    @Benchmark
    public void bitcheck(Blackhole bh){

        long tagBit = Config.POSTag.cp.getBit();
        // 사전의 tag 정보와 포함여부 tag 의 교집합 구함.
        long result = tagBit & Config.POSTag.cp.getBit();

        boolean check = (result > 0);

        bh.consume(check);
    }
}