package daon.analysis.ko.perf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.model.TestModel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@State(Scope.Benchmark)
public class AnalyzerPerfTest {

    TestModel model = new TestModel();

    @Setup
    public void setup() throws IOException, InterruptedException {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

        model.before();
    }

    @Benchmark
    public void testRead(Blackhole bh) throws IOException, InterruptedException {

        List<Term> results = model.read();

        bh.consume(results);
    }


}
