package daon.analysis.ko.fst;

import com.google.protobuf.ByteString;
import daon.analysis.ko.model.KeywordIntsRef;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;

import java.io.*;
import java.util.List;
import java.util.Set;
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

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        Builder<Object> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);

        //중복 제거, 정렬, output append
        for (KeywordIntsRef keywordIntsRef : keywordIntsRefs) {

            IntsRefBuilder curOutput = new IntsRefBuilder();

            KeywordIntsRef keyword = keywordIntsRef;

            if (keyword == null) {
                continue;
            }

            final IntsRef input = keyword.getInput();
            final int[] seqs = keyword.getSeqs();
            final long freq = keyword.getFreq();

            IntStream.of(seqs).forEach(curOutput::append);

            IntsRef wordSeqs = curOutput.get();

            PairOutputs.Pair<Long, IntsRef> pair = output.newPair(freq, wordSeqs);

            fstBuilder.add(input, pair);

            keyword.clearInput();
        }

        DaonFST<Object> fst = new DaonFST<>(fstBuilder.finish());

        return fst;
    }

    public DaonFST<IntsRef> buildIntsFst(List<KeywordIntsRef> keywordIntsRefs) throws IOException {

        IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();
        Builder<IntsRef> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);

        //중복 제거, 정렬, output append
        for (KeywordIntsRef keyword : keywordIntsRefs) {

            IntsRefBuilder curOutput = new IntsRefBuilder();

            if (keyword == null) {
                continue;
            }

            final IntsRef input = keyword.getInput();
            final int[] seqs = keyword.getSeqs();

            IntStream.of(seqs).forEach(curOutput::append);
            IntsRef wordSeqs = curOutput.get();

            fstBuilder.add(input, wordSeqs);

            keyword.clearInput();
        }

        DaonFST<IntsRef> fst = new DaonFST<>(fstBuilder.finish());

        return fst;
    }


    public DaonFST<Object> buildPairFst(byte[] bytes) throws IOException {

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = getPairOutput();

        return new DaonFST<>(byteToFst(bytes, fstOutput));
    }


    public DaonFST<IntsRef> buildIntsFst(byte[] bytes) throws IOException {

        IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();

        return new DaonFST<>(byteToFst(bytes, fstOutput));
    }


    public FST<Long> buildFst(byte[] bytes) throws IOException {

        PositiveIntOutputs fstOutput = PositiveIntOutputs.getSingleton();

        return byteToFst(bytes, fstOutput);
    }

    private <T> FST<T> byteToFst(byte[] bytes, Outputs<T> fstOutput) throws IOException {

        FST<T> readFst;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            readFst = new FST<T>(new InputStreamDataInput(new BufferedInputStream(is)), fstOutput);
        }

        return readFst;
    }



    public FST<Long> build(Set<IntsRef> set) throws IOException {

        PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
        final Builder<Long> builder = new Builder<>(FST.INPUT_TYPE.BYTE4, outputs);

        Long val = 1l;
        set.forEach(s ->{
            try {
                builder.add(s, val);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        FST<Long> fst = builder.finish();

        return fst;
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
