package daon.core.perf;

import lucene.core.util.IntsRefBuilder;
import lucene.core.util.fst.FST;
import lucene.core.util.fst.Util;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@State(Scope.Benchmark)
public class TestCode {

    private Map<Integer, Long> map = new HashMap<>();

    private FST<Long> fst = null;

    int wordSeq = 54717;
    int nInnerSeq = 144565;

    @Setup
    public void setup() throws IOException, InterruptedException {


    }



    @Benchmark
    public void testMap(Blackhole bh) throws IOException, InterruptedException {

        Integer key = (wordSeq + "|" + nInnerSeq).hashCode();
        Long cnt = map.get(key);

        bh.consume(cnt);
    }

    @Benchmark
    public void testFst(Blackhole bh) throws IOException, InterruptedException {

        IntsRefBuilder input = new IntsRefBuilder();

        input.append(wordSeq);
        input.append(nInnerSeq);

        Long cnt = Util.get(fst, input.get());

        bh.consume(cnt);
    }


    static class InnerInfo {
        private int wordSeq;
        private int nInnerSeq;
        private long cnt;


        public InnerInfo() {
        }

        public int getWordSeq() {
            return wordSeq;
        }

        public void setWordSeq(int wordSeq) {
            this.wordSeq = wordSeq;
        }

        public int getnInnerSeq() {
            return nInnerSeq;
        }

        public void setnInnerSeq(int nInnerSeq) {
            this.nInnerSeq = nInnerSeq;
        }

        public long getCnt() {
            return cnt;
        }

        public void setCnt(long cnt) {
            this.cnt = cnt;
        }
    }
}
