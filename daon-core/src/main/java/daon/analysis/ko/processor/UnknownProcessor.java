package daon.analysis.ko.processor;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.ResultInfo;
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

    public void process(ResultInfo resultInfo) {
        final char[] chars = resultInfo.getChars();

        //offset 별 기분석 사전 Term 추출 결과
//        TreeMap<Integer, Term> results = new TreeMap<>();

        UnknownInfo unknownInfo = new UnknownInfo(chars);

        List<ResultInfo.MissRange> missRanges = resultInfo.getMissRange();

        for(ResultInfo.MissRange missRange : missRanges){

            int offset = missRange.getOffset();
            int length = missRange.getLength();

            String word = new String(chars, offset, length);

            if(logger.isDebugEnabled()) {
                //addCandidateTerm unknown word
                logger.debug("unkown word : {}, offset : {}, length : {}", word, offset, length);
            }

            unknownInfo.setLength(length);
            unknownInfo.setOffset(offset);

            while (unknownInfo.next() != UnknownInfo.DONE) {

                int unknownOffset = offset + unknownInfo.current;
                int unknownLength = unknownInfo.end - unknownInfo.current;

                String unknownWord = new String(chars, unknownOffset, unknownLength);

                POSTag tag = getPosTag(unknownInfo);

                //미분석 keyword
                Keyword keyword = new Keyword(unknownWord, tag);

                ExplainInfo explainInfo = ExplainInfo.create().unknownMatch();

                Term term = new Term(unknownOffset, unknownLength, unknownWord, explainInfo, keyword);

//                results.put(unknownOffset, term);

                resultInfo.addCandidateTerm(term);
            }

            unknownInfo.reset();

        }

//        return results;
    }

    private POSTag getPosTag(UnknownInfo unknownInfo) {
        CharType type = unknownInfo.lastType;

        POSTag tag = POSTag.NNG;

        if(type == CharType.DIGIT){
            tag = POSTag.SN;
        }else if(type == CharType.LOWER || type == CharType.UPPER){
            tag = POSTag.SL;
        }

        return tag;
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
