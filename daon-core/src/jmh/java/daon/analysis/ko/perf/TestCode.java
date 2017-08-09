package daon.analysis.ko.perf;

import daon.analysis.ko.TestFst;
import daon.analysis.ko.reader.JsonFileReader;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.*;

@State(Scope.Benchmark)
public class TestCode {

    private Map<Integer, Long> map = new HashMap<>();

    private FST<Long> fst = null;

    int wordSeq = 54717;
    int nInnerSeq = 144565;

    @Setup
    public void setup() throws IOException, InterruptedException {
        PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
        final Builder<Long> builder = new Builder<>(FST.INPUT_TYPE.BYTE4, outputs);

        JsonFileReader reader = new JsonFileReader();

        List<InnerInfo> list = reader.read("/Users/mac/work/corpus/model/inner_info2.json", InnerInfo.class);

        Map<IntsRef,Long> fstData = new TreeMap<IntsRef,Long>();

        for(InnerInfo innerInfo : list){
            IntsRefBuilder input = new IntsRefBuilder();

            input.append(innerInfo.getWordSeq());
            input.append(innerInfo.getnInnerSeq());

            fstData.put(input.get(), innerInfo.getCnt());
        }

        for(Map.Entry<IntsRef,Long> e : fstData.entrySet()){
            builder.add(e.getKey(), e.getValue());
        }

        fst = builder.finish();

        for(InnerInfo innerInfo : list){
            Integer key = (innerInfo.getWordSeq() + "|" + innerInfo.getnInnerSeq()).hashCode();
            Long value = innerInfo.getCnt();

            map.put(key, value);

        }


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
