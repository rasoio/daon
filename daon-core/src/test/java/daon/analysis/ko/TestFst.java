package daon.analysis.ko;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.apache.lucene.util.fst.PairOutputs.Pair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class TestFst {


    private Logger logger = LoggerFactory.getLogger(TestFst.class);


    @Test
    /** like testShortestPathsRandom, but uses pairoutputs so we have both a weight and an output */
    public void testShortestPathsWFSTRandom() throws Exception {
        int numWords = 1000;

        final TreeMap<String,TwoLongs> slowCompletor = new TreeMap<>();
        final TreeSet<String> allPrefixes = new TreeSet<>();

        PairOutputs<Long,IntsRef> outputs = new PairOutputs<>(
                PositiveIntOutputs.getSingleton(), // wordId
                IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        final Builder<PairOutputs.Pair<Long,IntsRef>> builder = new Builder<>(FST.INPUT_TYPE.BYTE4, outputs);

        String random = "아버지";

        List<String> words = new ArrayList<>();
        words.add("아버지");
        words.add("어머니");

//        for (int i = 0; i < numWords; i++) {

//        words.forEach(word ->{

        for(int i=0;i<words.size();i++){
            String word = words.get(i);
            String s = word;

            for (int j = 1; j < s.length(); j++) {
                allPrefixes.add(word);
            }
            int weight = RandomUtils.nextInt(1, 100); // weights 1..100
            int output = RandomUtils.nextInt(0, 500); // outputs 0..500
            int output1 = RandomUtils.nextInt(0, 500); // outputs 0..500
            int output2 = RandomUtils.nextInt(0, 500); // outputs 0..500

            IntsRefBuilder scratch = new IntsRefBuilder();
            scratch.clear();
            scratch.append(output);
            scratch.append(output1);
            scratch.append(output2);

            IntsRef wordIds = scratch.get();

            logger.info("weight : {}, wordIds : {}", weight, wordIds.ints);

            slowCompletor.put(s, new TwoLongs(weight, wordIds));
        }

        final IntsRefBuilder scratch = new IntsRefBuilder();

        for (Map.Entry<String,TwoLongs> e : slowCompletor.entrySet()) {
            //System.out.println("add: " + e);
            long weight = e.getValue().a;
            IntsRef output = e.getValue().b;

            String word = e.getKey();

            scratch.clear();
//            scratch.grow(word.length());
//            scratch.setLength(word.length());

            for (int i = 0; i < word.length(); i++) {
//                scratch.setIntAt(i, (int) word.charAt(i));
                scratch.append((int) word.charAt(i));
            }

            IntsRef input = scratch.get();

//            Util.toIntsRef(new BytesRef(e.getKey()), scratch)

            builder.add(input, outputs.newPair(weight, output));
        }

        final FST<PairOutputs.Pair<Long,IntsRef>> fst = builder.finish();
        //System.out.println("SAVE out.dot");
        //Writer w = new OutputStreamWriter(new FileOutputStream("out.dot"));
        //Util.toDot(fst, w, false, false);
        //w.close();

        FST.BytesReader reader = fst.getBytesReader();

        //System.out.println("testing: " + allPrefixes.size() + " prefixes");
        for (String prefix : allPrefixes) {
            // 1. run prefix against fst, then complete by value
            //System.out.println("TEST: " + prefix);

            PairOutputs.Pair<Long,IntsRef> prefixOutput = outputs.getNoOutput();
            FST.Arc<PairOutputs.Pair<Long,IntsRef>> arc = fst.getFirstArc(new FST.Arc<>());
            for(int idx=0;idx<prefix.length();idx++) {

                if (fst.findTargetArc((int) prefix.charAt(idx), arc, arc, reader) == null) {
//                    fail();
                    break;
                }
                prefixOutput = outputs.add(prefixOutput, arc.output);


                // 매핑 종료
                if (arc.isFinal()) {

                    //사전 매핑 정보 output
                    final Pair<Long,IntsRef> wordSeqs = outputs.add(prefixOutput, arc.nextFinalOutput);

                    logger.info("output weight : {}, wordIds : {}",wordSeqs.output1, wordSeqs.output2.ints);
                }
            }

            final int topN = RandomUtils.nextInt(1, 10);

            Util.TopResults<PairOutputs.Pair<Long,IntsRef>> r = Util.shortestPaths(fst, arc, fst.outputs.getNoOutput(), minPairWeightComparator, topN, true);
            assertTrue(r.isComplete);
            // 2. go thru whole treemap (slowCompletor) and check its actually the best suggestion
            final List<Util.Result<PairOutputs.Pair<Long,IntsRef>>> matches = new ArrayList<>();

            /*
            // TODO: could be faster... but its slowCompletor for a reason
            for (Map.Entry<String,TwoLongs> e : slowCompletor.entrySet()) {
                if (e.getKey().startsWith(prefix)) {
                    //System.out.println("  consider " + e.getKey());
                    matches.add(new Util.Result<>(Util.toIntsRef(new BytesRef(e.getKey().substring(prefix.length())), new IntsRef()),
                            outputs.newPair(e.getValue().a - prefixOutput.output1, e.getValue().b - prefixOutput.output2)));
                }
            }

            assertTrue(matches.size() > 0);
            Collections.sort(matches, new TieBreakByInputComparator<>(minPairWeightComparator));
            if (matches.size() > topN) {
                matches.subList(topN, matches.size()).clear();
            }

            assertEquals(matches.size(), r.topN.size());

            for(int hit=0;hit<r.topN.size();hit++) {
                //System.out.println("  check hit " + hit);
                assertEquals(matches.get(hit).input, r.topN.get(hit).input);
                assertEquals(matches.get(hit).output, r.topN.get(hit).output);
            }
            */
        }
    }

    // compares just the weight side of the pair
    static final Comparator<Pair<Long,IntsRef>> minPairWeightComparator = new Comparator<Pair<Long,IntsRef>> () {
        @Override
        public int compare(Pair<Long,IntsRef> left, Pair<Long,IntsRef> right) {
            return left.output1.compareTo(right.output1);
        }
    };

    class TwoLongs {
        private long a;
        private IntsRef b;

        public TwoLongs(long a, IntsRef b) {
            this.a = a;
            this.b = b;
        }

        public long getA() {
            return a;
        }

        public void setA(long a) {
            this.a = a;
        }

        public IntsRef getB() {
            return b;
        }

        public void setB(IntsRef b) {
            this.b = b;
        }
    }


    @Test
    public void testListOfOutputs() throws Exception {
//        PositiveIntOutputs _outputs = PositiveIntOutputs.getSingleton();

        PairOutputs<Long,IntsRef> _outputs = new PairOutputs<>(
                PositiveIntOutputs.getSingleton(), // wordId
                IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<Pair<Long,IntsRef>> outputs = new ListOfOutputs<>(_outputs);
        final Builder<Object> builder = new Builder<>(FST.INPUT_TYPE.BYTE1, outputs);

        final Pair<Long,IntsRef> pair1 = getPair(_outputs, 1, new int[]{1,2,3});
        final Pair<Long,IntsRef> pair2 = getPair(_outputs, 2, new int[]{4,5});
        final Pair<Long,IntsRef> pair3 = getPair(_outputs, 3, new int[]{6,7,8,9,10});

        final IntsRefBuilder scratch = new IntsRefBuilder();

        builder.add(Util.toIntsRef(new BytesRef("a"), scratch), pair1);
        builder.add(Util.toIntsRef(new BytesRef("a"), scratch), pair2);
        builder.add(Util.toIntsRef(new BytesRef("a"), scratch), pair3);
        builder.add(Util.toIntsRef(new BytesRef("b"), scratch), pair3);
        final FST<Object> fst = builder.finish();

        Object output = Util.get(fst, new BytesRef("a"));
//        assertNotNull(output);
        List<Pair<Long,IntsRef>> outputList = outputs.asList(output);

        System.out.println(outputList);
//        assertEquals(3, outputList.size());
//        assertEquals(1L, outputList.get(0).longValue());
//        assertEquals(3L, outputList.get(1).longValue());
//        assertEquals(0L, outputList.get(2).longValue());

        output = Util.get(fst, new BytesRef("b"));
//        assertNotNull(output);
        outputList = outputs.asList(output);

        System.out.println(outputList);

//        assertEquals(1, outputList.size());
//        assertEquals(17L, outputList.get(0).longValue());
    }

    private Pair<Long,IntsRef> getPair(PairOutputs<Long, IntsRef> _outputs, long weight, int[] seq) {
        final IntsRefBuilder scratch = new IntsRefBuilder();
        // Add the same input more than once and the outputs
        // are merged:

        scratch.clear();
//            scratch.grow(word.length());
//            scratch.setLength(word.length());

        for (int i = 0; i < seq.length; i++) {
//                scratch.setIntAt(i, (int) word.charAt(i));
            scratch.append(seq[i]);
        }

        IntsRef wordSeqs = scratch.get();

        return _outputs.newPair(weight, wordSeqs);
    }
}
