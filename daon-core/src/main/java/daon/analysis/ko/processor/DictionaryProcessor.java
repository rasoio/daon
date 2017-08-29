package daon.analysis.ko.processor;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.config.MatchType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.DaonFST;
import daon.analysis.ko.model.*;
import daon.analysis.ko.util.Utils;
import lucene.core.util.IntsRef;
import lucene.core.util.fst.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Created by mac on 2017. 5. 18..
 */
public class DictionaryProcessor {

    private Logger logger = LoggerFactory.getLogger(DictionaryProcessor.class);

    private ModelInfo modelInfo;

    public static DictionaryProcessor create(ModelInfo modelInfo) {

        return new DictionaryProcessor(modelInfo);
    }

    private DictionaryProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    /**
     * 첫번째 문자열에 대해서 find
     *
     * 최종 결과 리턴 (최대 매칭 결과)
     * @param lattice
     * @throws IOException
     */
    public void process(Lattice lattice) throws IOException {

        char[] chars = lattice.getChars();
        List<EojeolInfo> eojeolInfos = lattice.getEojeolInfos();

        WhitespaceDelimiter whitespaceDelimiter = new WhitespaceDelimiter(chars);
        WordDelimiter wordDelimiter = new WordDelimiter(chars);

        while (whitespaceDelimiter.next() != WhitespaceDelimiter.DONE){
            int offset = whitespaceDelimiter.current;
            int length = whitespaceDelimiter.end - whitespaceDelimiter.current;

            String surface = new String(chars, offset, length);

            EojeolInfo eojeolInfo = new EojeolInfo();
            eojeolInfo.setSurface(surface);

            eojeolInfos.add(eojeolInfo);

            logger.debug("eojeol : {}, offset : {}, length : {}", surface, offset, length);

            wordDelimiter.setOffset(offset);
            wordDelimiter.setLength(length);

            while (wordDelimiter.next() != WordDelimiter.DONE) {

                int wordOffset = wordDelimiter.getOffset() + wordDelimiter.current;
                int wordLength = wordDelimiter.end - wordDelimiter.current;
                CharType lastType = wordDelimiter.lastType;

                if(lastType == CharType.ALPHA || lastType == CharType.HANJA || lastType == CharType.DIGIT) {
                    addFromEtc(lattice, offset, wordOffset, wordLength, lastType);
                }else{
                    addFromDic(lattice, offset, wordOffset, wordLength, lastType);
                }
            }

            wordDelimiter.reset();
        }

    }

    private void addFromDic(Lattice lattice, int offset, int wordOffset, int wordLength, CharType lastType) throws IOException {
        char[] chars = lattice.getChars();

        DaonFST<Object> fst = modelInfo.getWordFst();

        //unknown 탐색 정보
        Unknown unknown = new Unknown();

        //사전 찾기
        for(int pos = 0; pos < wordLength; pos++) {

            int findOffset = wordOffset + pos;
            int remaining = wordLength - pos;

            boolean isFirst = offset == findOffset;

            int findLength = findFST(fst, isFirst, findOffset, chars, remaining, lattice, wordLength);

//            if(isFirst && findLength == remaining){
            if(wordLength == findLength){
                break;
            }

            if(findLength == 0){
                unknown.add(findOffset);
            }
        }

        if(unknown.isExist()) {
            addUnknown(offset, lattice, unknown, lastType);
        }
    }

    private int findFST(DaonFST<Object> fst, boolean isFirst, int offset, char[] chars, int remaining, Lattice lattice, int wordLength) throws IOException {

        int findLength = 0;

        final FST.BytesReader fstReader = fst.getBytesReader();

        FST.Arc<Object> arc = new FST.Arc<>();
        arc = fst.getFirstArc(arc);
        Object output = fst.getOutputs().getNoOutput();

        for (int pos = 0; pos < remaining; pos++) {
            int ch = chars[offset + pos];

            //탐색 결과 없을때
            if (fst.findTargetArc(ch, arc, arc, pos == 0, fstReader) == null) {
                break; // continue to next position
            }

            //탐색 결과는 있지만 종료가 안되는 경우 == prefix 만 매핑된 경우
            output = fst.getOutputs().add(output, arc.output);

            // 매핑 종료
            if (arc.isFinal()) {

                //사전 매핑 정보 output
                Object outputs = fst.getOutputs().add(output, arc.nextFinalOutput);

                if(outputs != null){

                    List<PairOutputs.Pair<Long,IntsRef>> list = fst.asList(outputs);

                    //표층형 단어
                    final int length = (pos + 1);
                    final String word = new String(chars, offset, length);

                    boolean isMatchAll = wordLength == length;

                    //디버깅용 로깅
                    if(logger.isDebugEnabled()) {
                        logger.debug("fst word : {}, offset : {}, end : {}, find cnt : ({}), isMatchAll : {}", word, offset, (offset + length), list.size(), isMatchAll);

//                        debugWords(list);
                    }

                    //복합 키워드끼리 짤라서 로깅해야될듯
                    addNodes(lattice, isFirst, isMatchAll, offset, length, word, list);

                    findLength = length;
                }
            }

        }

        return findLength;
    }


    private void debugWords(List<PairOutputs.Pair<Long, IntsRef>> list) {
        list.sort((p1, p2) -> p2.output1.compareTo(p1.output1));

        for (PairOutputs.Pair<Long, IntsRef> pair : list) {
            List<Keyword> sb = new ArrayList<>();

            IntStream.of(pair.output2.ints).forEach(seq -> {

                Keyword k = modelInfo.getKeyword(seq);

                sb.add(k);
            });

            logger.debug("  freq : {}, keywords : {}, ints : {}", pair.output1, sb, pair.output2.ints);
        }
    }

    /**
     * 결과에 키워드 term 추가
     */
    private void addNodes(Lattice lattice, boolean isFirst, boolean isMatchAll, int offset, int length, String surface, List<PairOutputs.Pair<Long, IntsRef>> list) {

        for(PairOutputs.Pair<Long,IntsRef> pair : list){
            int[] findSeqs = pair.output2.ints;
            int cost = pair.output1.intValue();

//            Keyword[] keywords = IntStream.of(findSeqs)
//                    .mapToObj((int i) -> modelInfo.getKeyword(i))
//                    .toArray(Keyword[]::new);

            int size = findSeqs.length;
            Keyword[] keywords = new Keyword[size];
            for(int i =0; i<size; i++){
                keywords[i] = modelInfo.getKeyword(findSeqs[i]);
            }

            Node node = new Node(offset, length, surface, cost, MatchType.WORDS, keywords);

            if(isFirst){
                node.setFirst(true);
            }

            //해당 노드가 전체 매칭 인지 여부 체크
            if(isMatchAll){
                node.setMatchAll(true);
            }

            lattice.add(node);
        }
    }


    private void addFromEtc(Lattice lattice, int offset, int wordOffset, int wordLength, CharType lastType) {
        char[] chars = lattice.getChars();

        String word = new String(chars, wordOffset, wordLength);

        POSTag tag = getEtcPosTag(lastType);

        int seq = Utils.getSeq(tag);

        //미분석 keyword
        Keyword keyword = new Keyword(seq, word, tag);

        boolean isFirst = offset == wordOffset;

        Node node = new Node(wordOffset, wordLength, word, 0, MatchType.ETC, keyword);

        if(isFirst){
            node.setFirst(true);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("word : {} ({}), offset : {}, end : {}", word, lastType, wordOffset, (wordOffset + wordLength));
        }

        lattice.add(node);
    }

    private void addUnknown(int offset, Lattice lattice, Unknown unknown, CharType lastType) {
        List<Unknown.Position> unknowns = unknown.getList();
        //unknown's loop
        for (Unknown.Position p : unknowns){

            int unknownOffset = p.getOffset();
            int unknownLength = p.getLength();

            char[] chars = lattice.getChars();
            String unknownWord = new String(chars, unknownOffset, unknownLength);

            POSTag tag = POSTag.UNKNOWN;

            //charType ETC인 경우 기타기호 태그 처리 SW
            if(lastType == CharType.ETC){
                tag = POSTag.SW;
            }

            int seq = 0;

            //미분석 keyword
            Keyword keyword = new Keyword(seq, unknownWord, tag);

            boolean isFirst = offset == unknownOffset;

            Node node = new Node(unknownOffset, unknownLength, unknownWord, 0, MatchType.UNKNOWN, keyword);

            if(isFirst){
                node.setFirst(true);
            }

            if(logger.isDebugEnabled()) {
                logger.debug("unknown word : {}, offset : {}, end : {}", unknownWord, unknownOffset, (unknownOffset + unknownLength));
            }

            lattice.add(node);
        }
    }


    private POSTag getEtcPosTag(CharType type) {

        POSTag tag = POSTag.SW;

        if(type == CharType.DIGIT){
            tag = POSTag.SN;
        }else if(type == CharType.ALPHA){
            tag = POSTag.SL;
        }else if(type == CharType.HANJA){
            tag = POSTag.SH;
        }else{
            tag = POSTag.SW;
        }

        return tag;
    }


}
