package daon.analysis.ko.perf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.EojeolInfo;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.reader.ModelReader;
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

    private ModelInfo modelInfo;
    private DaonAnalyzer daonAnalyzer;

    @Setup
    public void setup() throws IOException, InterruptedException {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

        modelInfo = ModelReader.create().load();

        daonAnalyzer = new DaonAnalyzer(modelInfo);

    }

    @Benchmark
    public void testRead(Blackhole bh) throws IOException, InterruptedException {

        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String sentence = "거슬러 내려가셨다";
        List<EojeolInfo> eojeolInfos = daonAnalyzer.analyzeText(sentence);

        bh.consume(eojeolInfos);
    }

}
