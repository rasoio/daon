package daon.core.processor;

import daon.core.config.CharType;
import daon.core.config.MatchType;
import daon.core.config.POSTag;
import daon.core.fst.DaonFST;
import daon.core.model.*;
import daon.core.util.CharTypeChecker;
import daon.core.util.Utils;
import lucene.core.util.IntsRef;
import lucene.core.util.fst.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by mac on 2017. 5. 18..
 */
public class DictionaryProcessor {

    private Logger logger = LoggerFactory.getLogger(DictionaryProcessor.class);

    private ModelInfo modelInfo;
    private DaonFST<Object> fst;

    public static DictionaryProcessor create(ModelInfo modelInfo) {

        return new DictionaryProcessor(modelInfo);
    }

    private DictionaryProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;

        fst = modelInfo.getWordFst();
    }

    /**
     * 첫번째 문자열에 대해서 find
     *
     * 최종 결과 리턴 (최대 매칭 결과)
     * @param lattice lattice
     * @throws IOException exception
     */
    public void process(Lattice lattice) throws IOException {

        char[] chars = lattice.getChars();
        int length = lattice.getCharsLength();
        List<EojeolInfo> eojeolInfos = lattice.getEojeolInfos();

        addEojeols(lattice, chars, length, eojeolInfos);
    }

    private void addEojeols(Lattice lattice, char[] chars, int charsLength, List<EojeolInfo> eojeolInfos) throws IOException {
        WhitespaceDelimiter whitespaceDelimiter = new WhitespaceDelimiter(chars, charsLength);
        WordDelimiter wordDelimiter = new WordDelimiter(chars, charsLength);

        while (whitespaceDelimiter.next() != WhitespaceDelimiter.DONE){
            int offset = whitespaceDelimiter.current;
            int length = whitespaceDelimiter.end - whitespaceDelimiter.current;

            String surface = new String(chars, offset, length);

            EojeolInfo eojeolInfo = new EojeolInfo();
            eojeolInfo.setSurface(surface);

            eojeolInfos.add(eojeolInfo);

            if(logger.isDebugEnabled()) {
                logger.debug("eojeol : {}, offset : {}, length : {}", surface, offset, length);
            }

            addFromDic(lattice, offset, length, wordDelimiter);
        }
    }

    private void addFromDic(Lattice lattice, int offset, int length, WordDelimiter wordDelimiter) throws IOException {
        char[] chars = lattice.getChars();

        //unknown 탐색 정보
        Unknown unknown = new Unknown();

        //사전 찾기
        for(int pos = 0; pos < length; pos++) {

            int findOffset = offset + pos;
            int remaining = length - pos;

            boolean isFirst = offset == findOffset;

            int findLength = findFST(isFirst, findOffset, chars, remaining, lattice, length);

            if(length == findLength){
                break;
            }

            if(findLength == 0) {
//                logger.debug("unknown char : {}", chars[findOffset]);
                unknown.add(findOffset);
            }
        }

        if(unknown.isExist()) {
            addUnknown(offset, lattice, unknown, wordDelimiter);
        }
    }

    private int findFST(boolean isFirst, int offset, char[] chars, int remaining, Lattice lattice, int wordLength) throws IOException {

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

                    //타입별 처리 로직 추가

                    //타입이 영문, 한문, 숫자인 경우 전체 매칭이 아니면 미매칭으로 처리
                    CharType firstType = CharTypeChecker.charType(chars[offset]);

                    if(firstType == CharType.ALPHA || firstType == CharType.DIGIT || firstType == CharType.HANJA ){
                        if(!isMatchAll) return 0;
                    }

                    //디버깅용 로깅
                    if(logger.isDebugEnabled()) {
                        logger.debug("fst word : {}, offset : {}, end : {}, find cnt : ({}), isMatchAll : {}", word, offset, (offset + length), list.size(), isMatchAll);

                        debugWords(list);
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

            logger.debug("  cost : {}, keywords : {}, ints : {}", pair.output1, sb, pair.output2.ints);
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

        Node node = new Node(wordOffset, wordLength, word, 0, MatchType.UNKNOWN, keyword);

        if(isFirst){
            node.setFirst(true);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("word : {} ({}), offset : {}, end : {}", word, lastType, wordOffset, (wordOffset + wordLength));
        }

        lattice.add(node);
    }

    private void addUnknown(int offset, Lattice lattice, Unknown unknown, WordDelimiter wordDelimiter) {
        char[] chars = lattice.getChars();

        List<Unknown.Position> unknowns = unknown.getList();
        //unknown's loop
        for (Unknown.Position p : unknowns){

            int unknownOffset = p.getOffset();
            int unknownLength = p.getLength();

            if (logger.isDebugEnabled()) {
                String unknownWord = new String(chars, unknownOffset, unknownLength);
                logger.debug("unknownword : {}, offset : {}, end : {}", unknownWord, unknownOffset, unknownOffset + unknownLength);
            }

            wordDelimiter.setOffset(unknownOffset);
            wordDelimiter.setLength(unknownLength);

            while (wordDelimiter.next() != WordDelimiter.DONE) {
                int wordOffset = wordDelimiter.getOffset() + wordDelimiter.current;
                int wordLength = wordDelimiter.end - wordDelimiter.current;
                CharType lastType = wordDelimiter.lastType;

                addFromEtc(lattice, offset, wordOffset, wordLength, lastType);
            }

            wordDelimiter.reset();
        }
    }

    private POSTag getEtcPosTag(CharType type) {

        POSTag tag = POSTag.UNKNOWN;

        if(type == CharType.DIGIT){
            tag = POSTag.SN;
        }else if(type == CharType.ALPHA){
            tag = POSTag.SL;
        }else if(type == CharType.HANJA){
            tag = POSTag.SH;
        }else if(type == CharType.ETC){
            tag = POSTag.SW;
        }

        return tag;
    }


}
