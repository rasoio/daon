package daon.analysis.ko.fst;

import com.google.protobuf.ByteString;
import daon.analysis.ko.model.KeywordSeq;
import daon.analysis.ko.reader.ModelReader;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * FST build
 */
public class DaonFSTBuilder {


    public static DaonFSTBuilder create() {

        return new DaonFSTBuilder();
    }

    private DaonFSTBuilder() {}


    public DaonFST<Object> buildPairFst(List<KeywordSeq> keywordSeqs) throws IOException {

        //seq 별 Keyword
        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
                PositiveIntOutputs.getSingleton(), // word weight
                IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        Builder<Object> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);

        //중복 제거, 정렬, output append
        for (KeywordSeq keywordSeq : keywordSeqs) {

            IntsRefBuilder curOutput = new IntsRefBuilder();

            KeywordSeq keyword = keywordSeq;

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

    public DaonFST buildIntsFst(List<KeywordSeq> keywordSeqs) throws IOException {

        IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();
        Builder<IntsRef> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);

        //중복 제거, 정렬, output append
        for (KeywordSeq keyword : keywordSeqs) {

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

        return byteToFst(bytes, fstOutput);
    }


    public DaonFST<IntsRef> buildIntsFst(byte[] bytes) throws IOException {

        IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();

        return byteToFst(bytes, fstOutput);
    }

    private <T> DaonFST<T> byteToFst(byte[] bytes, Outputs<T> fstOutput) throws IOException {

        FST<T> readFst;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            readFst = new FST<T>(new InputStreamDataInput(new BufferedInputStream(is)), fstOutput);
        }

        return new DaonFST<>(readFst);
    }



    public DaonFST build(List<KeywordSeq> keywordSeqs) throws IOException {

        //seq 별 Keyword
        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
            PositiveIntOutputs.getSingleton(), // word weight
            IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        Builder<Object> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);

        //중복 제거, 정렬, output append
        for (int idx = 0, len = keywordSeqs.size(); idx < len; idx++) {

            IntsRefBuilder curOutput = new IntsRefBuilder();

            KeywordSeq keyword = keywordSeqs.get(idx);

            if (keyword == null) {
                continue;
            }

            final IntsRef input = keyword.getInput();
            final int[] seqs = keyword.getSeqs();
            final long freq = keyword.getFreq();

            IntStream.of(seqs).forEach(curOutput::append);

            IntsRef wordSeqs = curOutput.get();

            PairOutputs.Pair<Long,IntsRef> pair = output.newPair(freq, wordSeqs);

            fstBuilder.add(input, pair);

            keyword.clearInput();
        }

        DaonFST fst = new DaonFST(fstBuilder.finish());

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

        byte[] fstBytes = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            internalFST.save(new OutputStreamDataOutput(os));

            fstBytes = os.toByteArray();
        }

        return ByteString.copyFrom(fstBytes);
    }
}
