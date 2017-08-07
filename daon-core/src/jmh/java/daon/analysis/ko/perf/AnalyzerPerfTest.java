package daon.analysis.ko.perf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.*;
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.chasen.mecab.*;
import org.chasen.mecab.Lattice;
import org.chasen.mecab.Node;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@State(Scope.Benchmark)
public class AnalyzerPerfTest {

//    TestModel model = new TestModel();
    TestArcModel model = new TestArcModel();

    Model mecabModel = new Model();

    static {
        try {

            System.loadLibrary("MeCab");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Cannot load the example native code.\nMake sure your LD_LIBRARY_PATH contains \'.\'\n" + e);
            System.exit(1);
        }
    }

    @Setup
    public void setup() throws IOException, InterruptedException {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

        model.before();

    }

    @Benchmark
    @Fork(jvmArgsAppend = "-Djava.library.path=/usr/local/bin/mecab-java")
    public void testRead(Blackhole bh) throws IOException, InterruptedException {

//        List<Term> results = model.read();
//        List<EojeolInfo> results = model.read();

        String sentence = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String sentence = "거슬러 내려가셨다";
        List<EojeolInfo> eojeolInfos = model.getDaonAnalyzer().analyzeText2(sentence);

        bh.consume(eojeolInfos);
    }


    @Benchmark
    @Fork(jvmArgsAppend = "-Djava.library.path=/usr/local/bin/mecab-java")
    public void testReadMecab(Blackhole bh) throws IOException, InterruptedException {

        String str = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String str = "거슬러 내려가셨다";

        Lattice lattice = mecabModel.createLattice();
        Tagger tagger = mecabModel.createTagger();
        lattice.set_sentence(str);
        tagger.parse(lattice);
//        org.chasen.mecab.Node node = tagger.parseToNode(str);

//        List<Node> results = new ArrayList<>();
//        for (;node != null; node = node.getNext()) {
//            results.add(node);
//        }

        bh.consume(lattice.bos_node());
    }
}
