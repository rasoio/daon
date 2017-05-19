package daon.analysis.ko.perf;

import daon.analysis.ko.config.Config;
import daon.analysis.ko.config.POSTag;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class ConnectionMatrixPerfTest {

    private static Map<String, Float> results = new HashMap<String, Float>();
    private int size = 10 + 1;

    private float connectionMatrix[][] = new float[size][size];

    @Setup
    public void setup() {

        results.put("na|ps", 10f);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                connectionMatrix[i][j] = Float.MAX_VALUE;
            }
        }


    }

    @Benchmark
    public void get(Blackhole bh) {

        IntStream.range(0, 20).forEach(i -> {

            float score = results.get("na|ps");

            bh.consume(score);
        });
    }

    @Benchmark
    public void getArray(Blackhole bh) {

        IntStream.range(0, 20).forEach(i -> {

            float score = connectionMatrix[0][0];

            bh.consume(score);
        });
    }

    @Benchmark
    public void equals(Blackhole bh) {

        boolean check = POSTag.NNG.equals(POSTag.NNG);

        bh.consume(check);
    }



    @Benchmark
    public void equals2(Blackhole bh) {

        boolean check = (POSTag.NNG == POSTag.NNG);

        bh.consume(check);
    }

    @Benchmark
    public void bitcheck(Blackhole bh) {

        long tagBit = POSTag.NNG.getBit();
        // 사전의 tag 정보와 포함여부 tag 의 교집합 구함.
        long result = tagBit & POSTag.NNG.getBit();

        boolean check = (result > 0);

        bh.consume(check);
    }
}
