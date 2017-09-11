package daon.core.fst;

import com.google.protobuf.ByteString;
import daon.core.model.KeywordIntsRef;
import lucene.core.store.InputStreamDataInput;
import lucene.core.store.OutputStreamDataOutput;
import lucene.core.util.IntsRef;
import lucene.core.util.IntsRefBuilder;
import lucene.core.util.fst.*;
import lucene.core.util.packed.PackedInts;

import java.io.*;
import java.util.List;
import java.util.stream.IntStream;

/**
 * FST build
 */
public class DaonFSTBuilder {


    public static DaonFSTBuilder create() {

        return new DaonFSTBuilder();
    }

    private DaonFSTBuilder() {}


    public DaonFST<Object> buildPairFst(List<KeywordIntsRef> keywordIntsRefs) throws IOException {

        //seq 별 Keyword
        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
                PositiveIntOutputs.getSingleton(), // word weight
                IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> outputs = new ListOfOutputs<>(output);

//        Builder<Object> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);
        Builder<Object> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE4, 0, 0,
                true, true, Integer.MAX_VALUE, outputs,
                true, PackedInts.COMPACT, true, 15);

        //중복 제거, 정렬, output append
        for (KeywordIntsRef keyword : keywordIntsRefs) {

            IntsRefBuilder curOutput = new IntsRefBuilder();

            if (keyword == null) {
                continue;
            }

            final IntsRef input = keyword.getInput();
            final int[] seqs = keyword.getSeqs();
            long cost = keyword.getCost();

            //음수 불가..
            if(cost < 0){
                cost = 0;
            }

            IntStream.of(seqs).forEach(curOutput::append);

            IntsRef wordSeqs = curOutput.get();

            PairOutputs.Pair<Long, IntsRef> pair = output.newPair(cost, wordSeqs);

            fstBuilder.add(input, pair);

            keyword.clearInput();
        }

        DaonFST<Object> fst = new DaonFST<>(fstBuilder.finish());

        return fst;
    }

    public DaonFST<Object> buildPairFst(byte[] bytes) throws IOException {

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = getPairOutput();

        return new DaonFST<>(byteToFst(bytes, fstOutput));
    }

    private <T> FST<T> byteToFst(byte[] bytes, Outputs<T> fstOutput) throws IOException {

        FST<T> readFst;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            readFst = new FST<T>(new InputStreamDataInput(new BufferedInputStream(is)), fstOutput);
        }

        return readFst;
    }

    private ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> getPairOutput(){
        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
                PositiveIntOutputs.getSingleton(), // word weight
                IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> pairOutput = new ListOfOutputs<>(output);

        return pairOutput;
    }

    public static ByteString toByteString(DaonFST fst) throws IOException {
        FST internalFST = fst.getInternalFST();

        return toByteString(internalFST);
    }

    public static ByteString toByteString(FST fst) throws IOException {

        byte[] fstBytes = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            fst.save(new OutputStreamDataOutput(os));

            fstBytes = os.toByteArray();
        }

        return ByteString.copyFrom(fstBytes);
    }
}
