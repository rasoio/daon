package daon.core;

import daon.core.model.ModelInfo;
import lucene.core.util.IntsRef;
import lucene.core.util.IntsRefBuilder;
import lucene.core.util.fst.*;
import lucene.core.util.fst.PairOutputs.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static junit.framework.TestCase.assertTrue;

public class TestFst {


    private Logger logger = LoggerFactory.getLogger(TestFst.class);

    private ModelInfo modelInfo;


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
