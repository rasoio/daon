package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.score.BaseScorer;
import daon.analysis.ko.score.ScoreProperty;
import daon.analysis.ko.score.Scorer;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.FST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.CharType;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.fst.KeywordFST;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.dict.connect.ConnectMatrix;
import daon.analysis.ko.util.CharTypeChecker;

/**
 * Base class for a binary-encoded in-memory dictionary.
 */
public class BaseDictionary implements Dictionary {
	
	private Logger logger = LoggerFactory.getLogger(BaseDictionary.class);

	private KeywordFST fst;
	
	private ConnectMatrix connectMatrix;

	//원본 참조용 (idx, keyword)
	private List<KeywordRef> keywordRefs;

	private Scorer scorer;

	protected BaseDictionary(KeywordFST fst, List<KeywordRef> keywordRefs, ConnectMatrix connectMatrix) throws IOException {
		this.fst = fst; 
		this.keywordRefs = keywordRefs;

		this.connectMatrix = connectMatrix;
		ScoreProperty scoreProperty = new ScoreProperty();

		this.scorer = new BaseScorer(connectMatrix, scoreProperty);
	}

	@Override
	public void setConnectMatrix(ConnectMatrix connectMatrix) {
		this.connectMatrix = connectMatrix;
	}
	
	@Override
	public KeywordRef getKeywordRef(int idx){
		return keywordRefs.get(idx);
	}
	
	public ResultTerms lookup(char[] chars, int off, int len) throws IOException{
		
		ResultTerms results = lookupAll(chars, 0, len);

        return results;
	}


	/**
	 * 기분석 사전 참조
	 * @param chars
	 * @param offset
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public ResultTerms lookupAll(char[] chars, int offset, int len) throws IOException {

		//offset 별 기분석 사전 Term 추출 결과
		ResultTerms results = new ResultTerms(scorer);

		//미분석어절 처리용
		UnknownInfo unknownInfo = new UnknownInfo(chars);

		final FST.BytesReader fstReader = fst.getBytesReader();

		FST.Arc<IntsRef> arc = new FST.Arc<>();

		int end = offset + len;
		for (int startOffset = offset; startOffset < end; startOffset++) {
			arc = fst.getFirstArc(arc);
			IntsRef output = fst.getOutputs().getNoOutput();
			int remaining = end - startOffset;

			boolean isMatched = false;

			for (int i=0;i < remaining; i++) {
				int ch = chars[startOffset + i];

				CharType charType = CharTypeChecker.charType(ch);

				//탐색 결과 없을때
				if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
					break; // continue to next position
				}

				//탐색 결과는 있지만 종료가 안되는 경우 == prefix 만 매핑된 경우
				output = fst.getOutputs().add(output, arc.output);

				// 매핑 종료
				if (arc.isFinal()) {

				    //미분석 어절 처리
					if(unknownInfo.hasLength()){
						putUnknownTerm(chars, unknownInfo, results);
					}

					//사전 매핑 정보 output
					final IntsRef wordIds = fst.getOutputs().add(output, arc.nextFinalOutput);

					//표층형 단어
					final String word = new String(chars, startOffset, (i + 1));

					addTerms(startOffset, word, wordIds, charType, results);

					isMatched = true;
//					logger.info("offset : {}, word : {}", offset, word);
				}
			}

			//미분석 어절 시작점 설정
			if(isMatched == false){
                unknownInfo.addLength();
				if(!unknownInfo.hasOffset()){
					unknownInfo.setOffset(startOffset);
				}
			}

		}

		//마지막까지 미분석 어절인 경우
		if(unknownInfo.hasLength()){
			putUnknownTerm(chars, unknownInfo, results);
		}

		return results;
	}

    /**
     * 미분석어절 처리
     * @param chars
     * @param unknownInfo
     * @param results
     */
	private void putUnknownTerm(char[] chars, UnknownInfo unknownInfo, ResultTerms results){

        int start = unknownInfo.getOffset();

        while(unknownInfo.next() != UnknownInfo.DONE){

            int offset = start + unknownInfo.current;
            int length = unknownInfo.end - unknownInfo.current;

            String unknownWord = new String(chars, offset, length);

            POSTag tag = POSTag.un;

            //미분석 keyword
            Keyword keyword = new Keyword(unknownWord, tag);

            Term unknownTerm = createTerm(keyword, offset, length, unknownInfo.lastType);

            results.add(offset, unknownTerm);
        }

		unknownInfo.reset();
	}
	
	/**
	 * 결과에 키워드 term 추가
	 * @param offset
	 * @param word
	 * @param output
	 * @param type
	 * @param results
	 * @return
	 */
	private void addTerms(int offset, final String word, final IntsRef output, CharType type, ResultTerms results) {

		//wordId(seq)에 해당하는 Keyword 가져오기
		for(int i = 0; i < output.length; i++){
			int idx = output.ints[i];
			
			//ref 한개당 entry 한개.
			KeywordRef ref = getKeywordRef(idx);
			
			Keyword keyword;
			Keyword[] keywords = ref.getKeywords();
			
			//원본 사전 인 경우
			if(keywords.length == 1){
				keyword = keywords[0];
			}
			//조합 사전 인 경우
			else{
				Keyword cpKeyword = new Keyword(word, POSTag.cp);

				//Arrays.asList 이슈 될지..?
				List<Keyword> subWords = Arrays.asList(keywords);
				cpKeyword.setSubWords(subWords);
				
				keyword = cpKeyword;
			}
			
			int length = keyword.getWord().length();

			Term term = createTerm(keyword, offset, length, type);

			results.add(offset, term);
		}

	}

	private Term createTerm(Keyword keyword, int startOffset, int length, CharType type) {
		Term term = new Term(keyword, startOffset, length);

		term.setCharType(type);

		return term;
	}
	
	public List<KeywordRef> getData(){
		return keywordRefs;
	}

	class UnknownInfo {
		
		private char[] texts;
		
		private int length;
		private int offset;
		
		public int end;
		public int current;
		
		public CharType lastType;
		
		/** Indicates the end of iteration */
		public static final int DONE = -1;
		
		public UnknownInfo(char[] texts) {
			this.texts = texts;
			
			current = end = 0;
		}
		
		public void reset(){
			current = end = 0;
			offset = length = 0;
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
		
		public boolean isDone(){
			return end == DONE;
		}

		public boolean hasLength() {
			return length > 0;
		}

        public boolean hasOffset() {
            return offset > 0;
        }
	}
}
