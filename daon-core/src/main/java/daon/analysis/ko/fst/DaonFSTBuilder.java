package daon.analysis.ko.fst;

import com.google.protobuf.ByteString;
import daon.analysis.ko.model.KeywordSeq;
import daon.analysis.ko.reader.ModelReader;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * FST build
 */
public class DaonFSTBuilder {


    public static DaonFSTBuilder create() {

        return new DaonFSTBuilder();
    }

    private DaonFSTBuilder() {}

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
