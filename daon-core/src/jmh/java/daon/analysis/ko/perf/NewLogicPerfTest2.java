package daon.analysis.ko.perf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import daon.analysis.ko.model.CandidateTerm;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.model.TestModel;
import daon.analysis.ko.model.TestModel2;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@State(Scope.Benchmark)
public class NewLogicPerfTest2 {

//    TestModel model = new TestModel();
    TestModel2 model2 = new TestModel2();

    @Setup
    public void setup() throws IOException, InterruptedException {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

//        model.before();
        model2.before();
    }

//    @Benchmark
    public void testRead(Blackhole bh) throws IOException, InterruptedException {

//        List<Term> results = model.read();

//        bh.consume(results);
    }

    @Benchmark
    public void testRead2(Blackhole bh) throws IOException, InterruptedException {

        List<CandidateTerm> results = model2.read();

        bh.consume(results);
    }


}
