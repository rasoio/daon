package daon.analysis.ko.processor;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.model.ExplainInfo;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.util.CharTypeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mac on 2017. 5. 18..
 */
public class UnknownProcessor {

    private Logger logger = LoggerFactory.getLogger(UnknownProcessor.class);

    public static UnknownProcessor create() {
        return new UnknownProcessor();
    }

    private UnknownProcessor() {}

    public TreeMap<Integer, Term> process(char[] chars, int charsLength, TreeMap<Integer, List<Term>> dictionaryResults) {

        //offset 별 기분석 사전 Term 추출 결과
        TreeMap<Integer, Term> results = new TreeMap<>();

        UnknownInfo unknownInfo = new UnknownInfo(chars);

        for(int i =0; i< charsLength; i++) {

            List<Term> words = dictionaryResults.get(i);
//                logger.info("==> check idx : {}", i);

            if (words == null) {

                int offset = i;
                //다음 사전 단어의 위치
                int nextIdx = findNextIdx(offset, charsLength, dictionaryResults);
                int length = (nextIdx - offset);
                String word = new String(chars, offset, length);

                if(logger.isDebugEnabled()) {
                    //add unknown word
                    logger.debug("unkown starIdx : {}, length : {}, str : {}", offset, length, word);
                }


                //숫자 인 경우
                //영문 인 경우
                //조합 인 경우

                unknownInfo.setLength(length);
                unknownInfo.setOffset(offset);

                int start = unknownInfo.getOffset();

                while (unknownInfo.next() != UnknownInfo.DONE) {

                    int unknownOffset = offset + unknownInfo.current;
                    int unknownLength = unknownInfo.end - unknownInfo.current;

                    String unknownWord = new String(chars, unknownOffset, unknownLength);

                    CharType type = unknownInfo.lastType;

                    POSTag tag = POSTag.NNG;

                    if(type == CharType.DIGIT){
                        tag = POSTag.SN;
                    }else if(type == CharType.ALPHA){
                        tag = POSTag.SL;
                    }

                    //미분석 keyword
                    Keyword keyword = new Keyword(unknownWord, tag);

                    List<Keyword> keywords = new ArrayList<>(1);
                    keywords.add(keyword);

                    ExplainInfo explainInfo = ExplainInfo.create().unknownMatch();

                    Term term = new Term(unknownOffset, unknownLength, word, keywords, explainInfo);

                    results.put(offset, term);
                }

                unknownInfo.reset();

                i = nextIdx;
            }else{
                //마지막 word (가장 긴 단어)
                Term lastWord = words.get(words.size()-1);
                int length = lastWord.getLength();

                //다음 idx로
                i = i + (length - 1);
            }
        }

        return results;
    }

    private int findNextIdx(int offset, int length, TreeMap<Integer, List<Term>> eojeolResults) {

        int idx = offset + 1;

        for(; idx < length; idx++) {

            List<Term> words = eojeolResults.get(idx);

            if(words != null){
                break;
            }
        }

        return idx;
    }


    class UnknownInfo {

        private char[] texts;

        private int length;
        private int offset = DONE;

        public int end;
        public int current;

        public CharType lastType;

        /**
         * Indicates the end of iteration
         */
        public static final int DONE = -1;

        public UnknownInfo(char[] texts) {
            this.texts = texts;

            current = end = 0;
        }

        public void reset() {
            current = end = length = 0;
            offset = DONE;
        }

        public int getLength() {
            return length;
        }

        public void addLength() {
            this.length++;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int next() {
            // 현재 위치 설정. 이전의 마지막 위치
            current = end;

            if (current == DONE) {
                return DONE;
            }

            if (current >= length) {
                return end = DONE;
            }

            lastType = CharTypeChecker.charType(texts[offset + current]);

            // end 를 current 부터 1씩 증가.
            for (end = current + 1; end < length; end++) {

                CharType type = CharTypeChecker.charType(texts[offset + end]);

                // 마지막 타입과 현재 타입이 다른지 체크, 다르면 stop
                if (CharTypeChecker.isBreak(lastType, type)) {
                    break;
                }

                lastType = type;
            }

            return end;
        }

        public boolean isDone() {
            return end == DONE;
        }

        public boolean hasLength() {
            return length > 0;
        }

        public boolean hasOffset() {
            return offset >= 0;
        }
    }
}
