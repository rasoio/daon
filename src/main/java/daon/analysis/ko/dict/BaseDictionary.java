package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daon.analysis.ko.score.BaseScorer;
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

	protected BaseDictionary(KeywordFST fst, List<KeywordRef> keywordRefs) throws IOException {
		this.fst = fst; 
		this.keywordRefs = keywordRefs; 
	}

	@Override
	public void setConnectMatrix(ConnectMatrix connectMatrix) {
		this.connectMatrix = connectMatrix;
	}
	
	@Override
	public KeywordRef getKeywordRef(int idx){
		return keywordRefs.get(idx);
	}
	
	public List<Term> lookup(char[] chars, int off, int len) throws IOException{
		
		List<Term> bestTerms = new ArrayList<Term>();
		
		Map<Integer, List<Term>> map = lookupAll(chars, 0, len);

		Scorer scorer = new BaseScorer(connectMatrix);

		int loopCnt = 0;

		for(int idx=0; idx<len;){
			//기분석 사전 탐색 결과
			List<Term> terms = map.get(idx);
            int size = terms.size();

            //분석 결과가 한개인 경우 바로 적용
            if(size == 1){
                Term result = terms.get(0);
                
                //공백인 경우 추출 제외.
                if(!CharType.SPACE.equals(result.getCharType())){
                	bestTerms.add(result);
                }
                
                idx += result.getLength();
                continue;
            }

			//이전 분석결과...
			int resultSize = bestTerms.size();
			int lastIdx = resultSize-1;
			Term prevTerm = null;
			
			if(lastIdx > -1){
				prevTerm = bestTerms.get(lastIdx);
			}
			
			Term result = null;
			float resultScore = Integer.MAX_VALUE;

//            logger.info("curTerm start!!!!!!!!!!!!!!!!!!!!!!");

			for(Term curTerm : terms){


		        float curScore = scorer.score(prevTerm, curTerm);
				
//		        logger.info("prev : {}, cur : {} => score : {}", prevTerm, curTerm, curScore);
		        
		        //최소값 구하기
		        if(resultScore > curScore){
					result = curTerm;
					resultScore = curScore;
				}
				
				int offset = idx + curTerm.getLength();
				List<Term> nextTerms = map.get(offset);
				
				if(nextTerms != null){

				    for(Term nextTerm : nextTerms){

//				        logger.info("prev : {}, cur : {}, next : {}", prevTerm, curTerm, nextTerm);
				        loopCnt++;
                    }
                }else{

//                    logger.info("prev : {}, cur : {}, next : {}", prevTerm, curTerm, null);
                    loopCnt++;
                }

					//확률 스코어가 가장 큰 term을 추출
//					float curScore = curTerm.getScore();


//					scorer.score();


//					logger.info("===> curTerm : {}", curTerm);

//					if(nextTerms != null){
//						float maxScore = 0;
//						for(Term n : nextTerms){
//							float score = n.getScore();
//
//							if(maxScore < score){
//								maxScore = score;
//							}
////							logger.info("=======> n : {}", n);
//						}
//
//						curScore += maxScore;
//					}
//
//					if(resultScore < curScore){
//						result = curTerm;
//						resultScore = curScore;
//					}
			}

//            logger.info("curTerm start!!!!!!!!!!!!!!!!!!!!!!");

			bestTerms.add(result);
			idx += result.getLength();
        }


//        logger.info("##################################");
//        logger.info("loopCnt : {}", loopCnt);
//        logger.info("##################################");

        return bestTerms;
	}
	

	
	/**
	 * 기분석 사전 참조
	 * @param chars
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public Map<Integer, List<Term>> lookupAll(char[] chars, int off, int len) throws IOException {

		//offset 별 기분석 사전 Term 추출 결과
		Map<Integer, List<Term>> results = new HashMap<Integer, List<Term>>();
		
		//미분석어절 처리용
		UnknownInfo unknownInfo = new UnknownInfo(chars);
		int unknownLength = 0;
		int unknownOffset = 0;
		
		final FST.BytesReader fstReader = fst.getBytesReader();

		FST.Arc<IntsRef> arc = new FST.Arc<>();
		
		int end = off + len;
		for (int startOffset = off; startOffset < end; startOffset++) {
			arc = fst.getFirstArc(arc);
			IntsRef output = fst.getOutputs().getNoOutput();
			int remaining = end - startOffset;
			
			List<Term> terms = new ArrayList<Term>();
			String word = null;
			
			for (int i=0;i < remaining; i++) {
				int ch = chars[startOffset + i];
				
				CharType t = CharTypeChecker.charType(ch);
				
				//탐색 결과 없을때
				if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
					break; // continue to next position
				}
				
				//탐색 결과는 있지만 종료가 안되는 경우 == prefix 만 매핑된 경우
				output = fst.getOutputs().add(output, arc.output);
				
				// 매핑 종료
				if (arc.isFinal()) {
					
					final IntsRef wordIds = fst.getOutputs().add(output, arc.nextFinalOutput);

					word = new String(chars, startOffset, (i + 1));
					
					List<Term> ts = getTerms(startOffset, word, wordIds, t);
					
					//중복 offset 존재 누적 필요
					terms.addAll(ts);
					
					
					logger.info("offset : {}, word : {}", startOffset, word);
				}
			}
			
			
			results.put(startOffset, terms);
			
			//미분석 어절
			if(terms.size() == 0){
				unknownLength++;
				if(unknownOffset == 0){
					unknownOffset = startOffset;
				}
			}else{
				if(unknownLength > 0){
					putUnknownTerm(chars, results, unknownInfo, unknownLength, unknownOffset);
					
					unknownOffset = 0;
					unknownLength = 0;
				}
			}
			
		}

		//마지막까지 미분석 어절인 경우
		if(unknownLength > 0){
			putUnknownTerm(chars, results, unknownInfo, unknownLength, unknownOffset);
		}
		
		return results;
	}

	private void putUnknownTerm(char[] chars, Map<Integer, List<Term>> results, UnknownInfo unknownInfo, int unknownLength, int unknownOffset) {
		
		unknownInfo.reset();
		unknownInfo.setLength(unknownLength);
		unknownInfo.setStartIdx(unknownOffset);
		
		List<Term> unknownTerms = getUnkownTerms(chars, unknownInfo);
		
		for(Term t : unknownTerms){
			List<Term> uTerms = new ArrayList<Term>();
			
			uTerms.add(t);
			
			results.put(t.getOffset(), uTerms);
		}
	}
	
	/**
	 * 미분석 어절 분석
	 * @param texts
	 * @param unknownInfo
	 * @return
	 */
	private List<Term> getUnkownTerms(char[] texts, UnknownInfo unknownInfo) {
		
		List<Term> terms = new ArrayList<Term>();
		
		int start = unknownInfo.getStartIdx();
		
		while(unknownInfo.next() != UnknownInfo.DONE){
			
			int startOffset = start + unknownInfo.current;
			int length = unknownInfo.end - unknownInfo.current;
			
			String unkownWord = new String(texts, startOffset, length);
			
			POSTag tag = POSTag.un;
			
			//공백 문자 태그 설정?
			if(CharType.SPACE.equals(unknownInfo.lastType)){
				tag = POSTag.fin;
			}
			
			//미분석 keyword
			Keyword keyword = new Keyword(unkownWord, tag);
			
			Term unknowTerm = createTerm(keyword, startOffset, length, unknownInfo.lastType);
			
			terms.add(unknowTerm);
		}
		
		return terms;
	}

	private Term createTerm(Keyword keyword, int startOffset, int length, CharType type) {
		Term term = new Term(keyword, startOffset, length);

		term.setCharType(type);

		return term;
	}

	/**
	 * 결과에 키워드 term 추가
	 * @param startOffset
	 * @param word
	 * @param output
	 * @param type
	 * @return
	 */
	private List<Term> getTerms(int startOffset, String word, final IntsRef output, CharType type) {
		
		List<Term> terms = new ArrayList<Term>();
		
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

			Term term = createTerm(keyword, startOffset, length, type);
			
			terms.add(term);
		}
		
		return terms;
	}
	
	public List<KeywordRef> getData(){
		return keywordRefs;
	}

	class UnknownInfo {
		
		private char[] texts;
		
		private int length;
		private int startIdx;
		
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
		}

		public int getLength() {
			return length;
		}
		
		public void setLength(int length) {
			this.length = length;
		}
		
		public int getStartIdx() {
			return startIdx;
		}
		
		public void setStartIdx(int startIdx) {
			this.startIdx = startIdx;
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

			lastType = CharTypeChecker.charType(texts[startIdx+current]);

			// end 를 current 부터 1씩 증가.
			for (end = current + 1; end < length; end++) {

				CharType type = CharTypeChecker.charType(texts[startIdx+end]);

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
	}
}
