package daon.analysis.ko.perf;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

/**
 * Created by our_home on 2017-01-07.
 */
public class Test1 {
    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    public void wellHelloThere() {
        // this method was intentionally left blank.
    }
}
