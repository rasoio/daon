package daon.analysis.ko;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.config.MatchType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.DaonFST;
import daon.analysis.ko.model.*;
import daon.analysis.ko.processor.*;
import daon.analysis.ko.util.Utils;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PairOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class DaonAnalyzer implements Serializable{

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

    private ModelInfo modelInfo;


    public DaonAnalyzer(ModelInfo modelInfo) throws IOException {

        this.modelInfo = modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public List<EojeolInfo> analyzeText2(String text) throws IOException {
        List<EojeolInfo> eojeolInfos = new ArrayList<>();

        DaonFST<Object> fst = modelInfo.getWordFst();

        Lattice lattice = new Lattice(text);

        char[] chars = lattice.getChars();
        int charsLength = lattice.getCharsLength();
        WhitespaceDelimiter whitespaceDelimiter = new WhitespaceDelimiter(chars);
        WordDelimiter wordDelimiter = new WordDelimiter(chars);

        Connector connector = Connector.create(modelInfo);

        while (whitespaceDelimiter.next() != WhitespaceDelimiter.DONE){
            int offset = whitespaceDelimiter.current;
            int length = whitespaceDelimiter.end - whitespaceDelimiter.current;

            String eojeol = new String(chars, offset, length);


            EojeolInfo eojeolInfo = new EojeolInfo();
            eojeolInfo.setEojeol(eojeol);

            eojeolInfos.add(eojeolInfo);

            logger.debug("eojeol : {}, offset : {}, length : {}", eojeol, offset, length);

            wordDelimiter.setOffset(offset);
            wordDelimiter.setLength(length);

            while (wordDelimiter.next() != WordDelimiter.DONE) {

                int wordOffset = wordDelimiter.getOffset() + wordDelimiter.current;
                int wordLength = wordDelimiter.end - wordDelimiter.current;
                CharType lastType = wordDelimiter.lastType;


                if(lastType == CharType.ALPHA || lastType == CharType.HANJA || lastType == CharType.DIGIT) {
                    String word = new String(chars, wordOffset, wordLength);

                    POSTag tag = getPosTag(lastType);

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
                }else{
                    // 사전 탐색
                    Unknown unknown = new Unknown();

                    //한글 범위만 find 필요, 특문은 따로 처리
                    for(int pos = 0; pos < wordLength; pos++) {

                        int findOffset = wordOffset + pos;
                        int remaining = wordLength - pos;

                        boolean isFirst = offset == findOffset;


                        // TODO 어절의 시작 노드 표기 필요, pos == 0
                        // 전체 매칭 시 전체 매칭 결과만 사용 하도록 수정
                        // 전체 매칭 체크 조건 길이 체크 및 체크 시 특수문자 제외 처리
                        int findLength = findFST(fst, isFirst, findOffset, chars, remaining, lattice);

                        if(isFirst && findLength == remaining){
                            break;
                        }

                        if(findLength == 0){
                            unknown.add(findOffset);
                        }
                    }

                    findUnknown(offset, lattice, unknown);
                }
            }

            wordDelimiter.reset();

        }


        Node[] endNodes = lattice.getEndNodes();

        for(int pos = 0; pos < charsLength; pos++) {
            if(endNodes[pos] != null){

                Node rnode = getRightNode(pos, lattice);

                for (;rnode != null; rnode = rnode.getBeginNext()) {

                    int best_cost = 2147483647;
                    Node best_node = null;

                    for (Node lnode = endNodes[pos]; lnode != null; lnode = lnode.getEndNext()) {

                        if(lnode.getType() != MatchType.BOS && lnode.getPrev() == null){
                            logger.debug("prev is null lnode : {} : ({}), rnode : {} : ({}), cost : {}",
                                    lnode.getSurface(), lnode.getKeywords(),
                                    rnode.getSurface(), rnode.getKeywords(),
                                    lnode.getCost());
                            continue;
                        }

                        int lcost = connector.cost(lnode, rnode);
                        int cost = lnode.getCost() + lcost; // cost 값은 누적

                            logger.debug("lnode : {} : ({}), rnode : {} : ({}), cost : {}",
                                    lnode.getSurface(), lnode.getKeywords(),
                                    rnode.getSurface(), rnode.getKeywords(),
                                    cost);
                        //best 는 left node 중 선택, 즉 prev 설정
                        if (cost < best_cost) {
                            best_node  = lnode;
                            best_cost  = cost;

                        }

                    }

                    rnode.setPrev(best_node);
                    rnode.setCost(best_cost);
               }
            }
        }

        //eos 만들기
        Node eos = new Node(0,0, "EOS", MatchType.EOS);

//        //마지막 결과와 eos간의 connection 계산 한번만 실행
        for (int pos = charsLength; pos >= 0; --pos) {
            if (endNodes[pos] != null) {

                Node rnode = eos;
                int best_cost = 2147483647;
                Node best_node = null;

                for (Node lnode = endNodes[pos]; lnode != null; lnode = lnode.getEndNext()) {

                    if(lnode.getPrev() == null){
                        continue;
                    }

                    int lcost = connector.cost(lnode, rnode);
                    int cost = lnode.getCost() + lcost; // cost 값은 누적

                    //best 는 left node 중 선택, 즉 prev 설정
                    if (cost < best_cost) {
                        best_node  = lnode;
                        best_cost  = cost;

                        logger.debug("last eos lnode : {} : ({}), rnode : {} : ({}), cost : {}",
                                lnode.getSurface(), lnode.getKeywords(),
                                rnode.getSurface(), rnode.getKeywords(),
                                cost);
                    }

                }

                rnode.setPrev(best_node);
                rnode.setCost(best_cost);

                break;
            }
        }

        // backtrace next 연결
//        Node *node = lattice->eos_node(); // begin_codes 의 마지막 idx

        Node node = eos;
        for (Node prev_node; node.getPrev() != null;) {
            prev_node = node.getPrev();
            prev_node.setNext(node);
            node = prev_node;
        }

        int idx = 0;
        EojeolInfo eojeolInfo = null;
        for (Node n = node.getNext(); n.getNext() != null; n = n.getNext()){

            if(n.isFirst()){
                eojeolInfo = eojeolInfos.get(idx);
                idx++;
            }

            eojeolInfo.addNode(n);

            logger.debug("result node : {} : ({},{}) : ({}) : cost : {}, isFirst : {}", n.getSurface(), n.getOffset(), n.getLength(), n.getKeywords(), n.getCost(), n.isFirst());
        }

        return eojeolInfos;
    }

    private void findUnknown(int offset, Lattice lattice, Unknown unknown) {
        List<Unknown.Position> unknowns = unknown.getList();
        //unknown's loop
        for (Unknown.Position p : unknowns){

            int unknownOffset = p.getOffset();
            int unknownLength = p.getLength();

            char[] chars = lattice.getChars();
            String unknownWord = new String(chars, unknownOffset, unknownLength);

            POSTag tag = POSTag.UNKNOWN;

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


    public Node getRightNode(int offset, Lattice lattice){
        for(int pos = offset; pos< lattice.getCharsLength(); pos++) {
            Node node = lattice.getStartNode(pos);

            if(node != null){
                return node;
            }
        }

        return null;
    }


    private int findFST(DaonFST<Object> fst, boolean isFirst, int offset, char[] chars, int remaining, Lattice lattice) throws IOException {

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

                    boolean isMatchAll = isFirst && remaining == length;

                    //디버깅용 로깅
                    if(logger.isDebugEnabled()) {
                        logger.debug("fst word : {}, offset : {}, end : {}, find cnt : ({}), isMatchAll : {}", word, offset, (offset + length), list.size(), isMatchAll);

//                        debugWords(list);
                    }


                    //복합 키워드끼리 짤라서 로깅해야될듯
                    addTerms(lattice, isFirst, isMatchAll, offset, length, word, list);

                    findLength = length;
                }
            }

        }

        return findLength;
    }


    private void debugWords(List<PairOutputs.Pair<Long, IntsRef>> list) {
        list.sort((p1, p2) -> p2.output1.compareTo(p1.output1));

        for (PairOutputs.Pair<Long, IntsRef> pair : list) {
            List sb = new ArrayList();

            IntStream.of(pair.output2.ints).forEach(seq -> {

                Keyword k = modelInfo.getKeyword(seq);

                sb.add(k);
            });

//            logger.debug("  freq : {}, keywords : {}", pair.output1, sb);
        }
    }

    /**
     * 결과에 키워드 term 추가
     */
    private void addTerms(Lattice lattice, boolean isFirst, boolean isMatchAll, int offset, int length, String surface, List<PairOutputs.Pair<Long, IntsRef>> list) {

        for(PairOutputs.Pair<Long,IntsRef> pair : list){
            int[] findSeqs = pair.output2.ints;
            int wcost = pair.output1.intValue();

            Keyword[] keywords = IntStream.of(findSeqs)
                    .mapToObj((int i) -> modelInfo.getKeyword(i))
                    .filter(Objects::nonNull).toArray(Keyword[]::new);

            Node node = new Node(offset, length, surface, wcost, MatchType.WORDS, keywords);

            if(isFirst){
                node.setFirst(true);
            }

            if(isMatchAll){
                node.setMatchAll(true);
            }

            //해당 노드가 전체 어절 매칭 인지 여부 체크

            lattice.add(node);
        }
    }



    private POSTag getPosTag(CharType type) {

        POSTag tag = POSTag.NNG;

        if(type == CharType.DIGIT){
            tag = POSTag.SN;
        }else if(type == CharType.ALPHA){
            tag = POSTag.SL;
        }else if(type == CharType.HANJA){
            tag = POSTag.SH;
        }

        return tag;
    }


    public List<EojeolInfo> analyzeText(String text) throws IOException {

        List<EojeolInfo> eojeolInfos = new ArrayList<>();

        //공백 기준 분리
        String[] eojeols = text.split("\\s");

        //특수문자 분리

        //이전 어절 결과
        ResultInfo prevResultInfo = null;
        //다음 어절 결과
        ResultInfo nextResultInfo = null;

        //tokenizer results
        int length = eojeols.length;
        for(int i=0; i < length; i++){

            boolean isFirst = i == 0;
            boolean isLast = i == (length -1);

            String eojeol = eojeols[i];

            EojeolInfo info = new EojeolInfo();

            ResultInfo resultInfo = createResultInfo(eojeol);

            if(isFirst) {
                fillProcess(resultInfo);
            }else{
                resultInfo = nextResultInfo;
            }

            //다음 어절 구성
            if(isLast){
                nextResultInfo = null;
            }else{
                String nextEojeol = eojeols[i+1];
                nextResultInfo = createResultInfo(nextEojeol);
                fillProcess(nextResultInfo);
            }

            connectionProcess(prevResultInfo, resultInfo, nextResultInfo);

//            List<Term> terms = resultInfo.getTerms();
            List<Term> terms = resultInfo.getBestFilterSet().getTerms();

            prevResultInfo = resultInfo;

            info.setEojeol(eojeol);
            info.setTerms(terms);

            eojeolInfos.add(info);
        }

        return eojeolInfos;
    }

    private ResultInfo createResultInfo(String eojeol){
        char[] chars = eojeol.toCharArray();
        int length = chars.length;

        return ResultInfo.create(chars, length);
    }

    private void fillProcess(ResultInfo resultInfo) throws IOException {
        //사전 탐색 결과
        DictionaryProcessor.create(modelInfo).process(resultInfo);

        //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
        UnknownProcessor.create().process(resultInfo);

        CandidateFilterProcessor.create(modelInfo).process(resultInfo);
    }

    private void connectionProcess(ResultInfo beforeResult, ResultInfo resultInfo, ResultInfo nextResult) throws IOException {

        ConnectionProcessor.create(modelInfo).process(beforeResult, resultInfo, nextResult);

    }

}
