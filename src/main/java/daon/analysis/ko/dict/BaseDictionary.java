package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import daon.analysis.ko.util.CharTypeChecker;

/**
 * Base class for a binary-encoded in-memory dictionary.
 */
public class BaseDictionary implements Dictionary {
	
	private Logger logger = LoggerFactory.getLogger(BaseDictionary.class);

	private KeywordFST fst;

	//원본 참조용 (idx, keyword)
	private List<KeywordRef> keywordRefs;

	protected BaseDictionary(KeywordFST fst, List<KeywordRef> keywordRefs) throws IOException {
		this.fst = fst; 
		this.keywordRefs = keywordRefs; 
	}
	
	@Override
	public KeywordRef getKeywordRef(int idx){
		return keywordRefs.get(idx);
	}
	
	public List<Term> lookup(char[] chars, int off, int len) throws IOException{
		
		List<Term> bestTerms = new ArrayList<Term>();
		
		for(int idx=0; idx<len;){
			//기분석 사전 탐색 결과
			List<Term> terms = findDic(chars, idx, len);

			int size = terms.size();

//			logger.info("===========> idx : {}, terms : {}", idx, terms);

			int resultSize = bestTerms.size();
			int lastIdx = resultSize-1;
			Term prevTerm = null;
			
			if(lastIdx > -1){
				prevTerm = bestTerms.get(lastIdx);
			}
			
			// 추출 결과가 없는 경우 (미등록어 처리)
			if(size == 0){
				idx = lookupUnknown(chars, idx, len, bestTerms);
			}
			//결과가 한개인 경우 그냥 추출.
			else if(size == 1){
				Term result = terms.get(0);
				
				bestTerms.add(result);
				idx += result.getLength();
				
			//결과가 여러개 인경우 최대 스코어만 추출 
			}else{

				Term result = null;
				
				for(Term curTerm : terms){
					
					curTerm.setPrevTerm(prevTerm);

					logger.info("============> curTerm : {}", curTerm);
					
					if(result == null){
						result = curTerm;
					}
					
					int rlen = result.getLength();
					
					/*
					//같은 idx 값인 경우 재사용 필요.. 
					List<Term> nextTerms = justLookupOnly(chars, idx + rlen, len);
					
					for(Term nt : nextTerms){

//						logger.info("next t : {}", nt);
					}
					*/
					
					//확률 스코어가 가장 큰 term을 추출
					if(result.getScore() < curTerm.getScore()){
						result = curTerm;
					}
				}
				
				bestTerms.add(result);
				idx += result.getLength();
			}
		}
		
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
	private List<Term> findDic(char[] chars, int off, int len) throws IOException {
		List<Term> terms = new ArrayList<Term>();
		
		final FST.BytesReader fstReader = fst.getBytesReader();

		FST.Arc<IntsRef> arc = new FST.Arc<>();
		
		int end = len;

		int startOffset = off;

		//남은 길이
		int remaining = end - startOffset;
		
		arc = fst.getFirstArc(arc);
		IntsRef output = fst.getOutputs().getNoOutput();

		//offset 부터 끝까지 한글자씩 앞으로 이동
		for (int i = 0; i < remaining; i++) {
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

				final String word = new String(chars, startOffset, (i + 1));
				
				List<Term> ts = getTerms(startOffset, word, wordIds, t);
				
				terms.addAll(ts);
			}
		}
		
		return terms;
	}

	private int lookupUnknown(char[] chars, int off, int len, List<Term> bestTerms) throws IOException {
		UnknownInfo unknownInfo = new UnknownInfo(chars);
		
		int remaining = len - off;
		
		int length = 0;
		
		//미등록 어절을 구함
		for (int i = 1; i < remaining; i++) {
			List<Term> ts = findDic(chars, off + i, len);
			
			//기분석 결과를 찾은 경우
			if(ts.size() > 0){
				length = i;
				break;
			}
		}
		
		//마지막까지 기분석 결과를 못찾은 경우 남은 길이 전체를 길이로 할당
		if(length == 0){
			length = remaining;
		}
		
		unknownInfo.setLength(length);
		unknownInfo.setStartIdx(off);
		
		List<Term> unknownTerms = getUnkownTerms(chars, unknownInfo);
		
		for(Term t : unknownTerms){
//			logger.info("============> un t : {}", t);
			
			bestTerms.add(t);
			
			off += t.getLength();
		}
		
		return off;
	}
	
	private List<Term> getUnkownTerms(char[] texts, UnknownInfo unknownInfo) {
		//기분석 결과에 없는 경우 다음 음절 체크
		List<Term> terms = new ArrayList<Term>();
		
		int start = unknownInfo.getStartIdx();
		
		while(unknownInfo.next() != UnknownInfo.DONE){
			
			int startOffset = start + unknownInfo.current;
			int length = unknownInfo.end - unknownInfo.current;
			
			String unkownWord = new String(texts, startOffset, length);
			
//			logger.info("innder unknown word : '{}', startOffset : {}, length : {}, all : '{}'", unkownWord, startOffset, length, new String(unknownInfo.texts, unknownInfo.startIdx, unknownInfo.length));
			
			//미분석 keyword
			Keyword keyword = new Keyword(unkownWord, POSTag.un);
			Term unknowTerm = new Term(keyword, startOffset, length);
			unknowTerm.setCharType(unknownInfo.lastType);
			unknowTerm.setTag(POSTag.valueOf(keyword.getTag()));	
			
			terms.add(unknowTerm);
		}
		
		return terms;
	}

	/**
	 * 결과에 키워드 term 추가
	 * @param results
	 * @param startOffset
	 * @param t 
	 * @param wordId
	 */
	private List<Term> getTerms(int startOffset, String word, final IntsRef output, CharType t) {
		
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
			
			int offset = startOffset;
			int length = keyword.getWord().length();

			Term term = new Term(keyword, offset, length);
			term.setCharType(t);
			
			//복합 사전인 경우..
			String tag = keyword.getTag();
			if(tag.length() == 4){
				tag = tag.substring(0, 2);
			}
			
			term.setTag(POSTag.valueOf(tag));	
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
