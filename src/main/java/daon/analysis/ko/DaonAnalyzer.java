package daon.analysis.ko;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.config.Config.CharType;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.tag.Tag;
import daon.analysis.ko.util.CharTypeChecker;

public class DaonAnalyzer {

	private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);

	private Dictionary dictionary;
	
	private Tag tag;
	
	public DaonAnalyzer(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public ResultTerms analyze(String text) throws IOException{
		//원본 문자
		char[] texts = text.toCharArray();
		
		//총 길이
		int textLength = text.length();

		//기분석 사전 매칭 정보 가져오기
		Map<Integer, List<Term>> lookupResults = dictionary.lookup(texts, 0, textLength);
		
//		logger.info("text : {}", text);
		
//		우리말은 제2 유형인 SOV에 속한다. 따라서 국어의 기본 어순은 ‘주어+목적어+서술어’이다.
		// 1순위) NN 맨 앞 + NN + (J) + V + (E)
		// 2순위) M 맨 앞 + V + (E)
		// 3순위) V 맨 앞
		
		parse(lookupResults);
		
		
		
		//결과
		ResultTerms results = new ResultTerms(lookupResults);
		
		
		
		//원본 텍스트 길이 만큼 반복
		for(int idx=0; idx<textLength;){
			
			//idx에 해당하는 기분석 사전 결과 가져오기
			List<Term> currentTerms = results.get(idx);
			
//			System.out.println(idx + " - " + texts[idx] + " : " + currentTerms);

			/**
			 * 품사 결합 조건 체크
			 */
			//이전 추출 term 가져오기 
			Term prevTerm = results.getPrevTerm();
			
			//이전 기분석 사전 추출 term과 현재 기분석 사전 추추 term이 존재
			if(prevTerm != null && currentTerms != null){
				
				//이전 추출 term 이 체언(명사)인 경우 뒤에 조사/어미 여부를 체크
				if(isNoun(prevTerm)){
					
					//현재 기분석 사전 추출 term 들중 어느것을 가져와야될지..?
					Term t = lastTerm(currentTerms);
					if(isJosa(t)){
						prevTerm.setTag(POSTag.n);
						
						t.setTag(POSTag.p);
						
						//뽑힌 근거 추가.
						results.add(t);
						
						idx += t.getLength();
						
						continue;
					}
					
					if(isSuffix(t)){
						prevTerm.setTag(POSTag.n);
						
						t.setTag(POSTag.x);
						
						//뽑힌 근거 추가.
						results.add(t);
						
						idx += t.getLength();
						
						continue;
					}
				}
				
				//용언(동사) 인 경우 어미 여부 체크 ?
				if(isVerb(prevTerm)){
					
					Term t = firstTerm(currentTerms);
					if(isEomi(t)){

						t.setTag(POSTag.e);
						
						results.add(t);

						idx += t.getLength();
						
						continue;
					}
				}
				
				//접미사 인 경우 
				if(isSuffix(prevTerm)){
					Term t = firstTerm(currentTerms);
					if(isVerb(t)){

						//공통 함수 추출 -> 현재 텀에 품사 태깅 후 result 설정 idx 증가 
						t.setTag(POSTag.v); // <- 확정 된 품사에 일치하는 기분석 사전의 attr 품사 가져오기 
						
						results.add(t);

						idx += t.getLength();
						
						continue;
					}
					
				}
			}
			
			//현재 기분석 term이 존재하는 경우
			if(currentTerms != null){
				//좌 -> 우 최장일치법 or 최적 수치 사용?
				//마지막이 제일 긴 term 
				//선택되는 근거..? 
				Term lastTerm = lastTerm(currentTerms);
				int length = lastTerm.getLength();
				
				results.add(lastTerm);
				idx += length;
			}
			//미분석 term 처리
			else{
				Term unkownTerm = makeUnkownTerm(idx, texts, textLength, lookupResults);

				int length = unkownTerm.getLength();

				results.add(unkownTerm);
				idx += length;
				
				logger.info("is call???? term : {}", unkownTerm);
			}
			
		}
		
		
		return results;
	}
	
	private void parse(Map<Integer, List<Term>> lookupResults) {

		lookupResults.entrySet().stream().forEach(e -> {
			int startOffset = e.getKey();
			
			e.getValue().stream().forEach(t->{
				Keyword cur = t.getKeyword();
				String tag = cur.getTag();
				int len = cur.getWord().length();
				int position = startOffset + len;
				
				
				List<Term> terms = lookupResults.get(position);

//				logger.info("startOffset : {}, position : {}, terms : {}", startOffset, position, terms);
				
				if(terms != null){
					for(Term n : terms){
						
//						if(n.getCharType().equals(CharType.SPACE)){
//							logger.info("cur : {}, space: {}", cur, n);
//						}
						
						Keyword next = n.getKeyword();
						
						String nextTag = next.getTag();
						
						String cmb = tag + nextTag;
						boolean isValid = this.tag.isValid(cmb);
	
						if(isValid){
							logger.info("valid : {}, cur : {}, next: {}", isValid, t, n);
						}
					}
				}
			});
		});
	}

	private Term firstTerm(List<Term> terms) {
		if(terms == null){
			return null;
		}
		
		Term t = terms.get(0);
		return t;
	}
	
	private Term lastTerm(List<Term> terms) {
		if(terms == null){
			return null;
		}
		
		int lastIdx = terms.size() -1;
		Term t = terms.get(lastIdx);
		
		return t;
	}
	
	
	/**
	 * 분리 예정 소스 
	 */
	
	
	/**
	 * 미분석 어절 구성
	 * 
	 * TODO 영문, 숫자, 한글 타입 구분
	 * TODO 공백 문자 처리
	 * 
	 * @param idx
	 * @param texts
	 * @param textLength
	 * @param lookupResults
	 * @return
	 */
	private Term makeUnkownTerm(int idx, char[] texts, int textLength, Map<Integer, List<Term>> lookupResults) {
		//기분석 결과에 없는 경우 다음 음절 체크
		int startIdx = idx;
		int endIdx = startIdx;
		
		char c = texts[endIdx];
		
		CharType lastType = CharTypeChecker.charType(c);
		
		for(; endIdx < textLength; endIdx++){
			
			//타입 체크용, 현재 문자의 타입
			c = texts[endIdx];
			CharType type = CharTypeChecker.charType(c);
			
			List<Term> terms = lookupResults.get(endIdx);
			
			if(terms != null){
				break;
			}
			
			if(CharTypeChecker.isBreak(lastType, type)){
				break;
			}
			
			lastType = type;
		}

		int length = (endIdx - startIdx);
		
		String unkownWord = new String(texts, startIdx, length);
		
		//미분석 keyword
		Keyword word = new Keyword(unkownWord, "un");
		Term unknowTerm = new Term(word, startIdx, length);
		unknowTerm.setCharType(lastType);
		
		return unknowTerm;
	}

	
	

	private boolean isNoun(Term t){
		
		return contains(t, POSTag.n);
	}

	private boolean isJosa(Term t){
		
		return contains(t, POSTag.p);
	}

	private boolean isSuffix(Term t){
		
		return contains(t, POSTag.x);
	}



	private boolean isVerb(Term t){
		
		return contains(t, POSTag.v);
	}

	private boolean isEomi(Term t){
		
		return contains(t, POSTag.e);
	}
	
	private boolean contains(Term t, POSTag tag){
		boolean is = false;
		
		//확정 된 품사 우선 체크
		if(t.getTag() != null){
			long result = t.getTag().getBit() & tag.getBit();
			
			if(result > 0){
				return true;
			}
		}else if(t != null && t.getKeyword() != null){
			
			long tagBits = t.getKeyword().getTagBit();
			// 사전의 tag 정보와 포함여부 tag 의 교집합 구함.
			long result = tagBits & tag.getBit();
			
			if(result > 0){
				return true;
			}
		}
		
		return is;
	}
}
