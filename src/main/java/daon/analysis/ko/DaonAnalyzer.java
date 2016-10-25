package daon.analysis.ko;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;

public class DaonAnalyzer {

	private Logger logger = LoggerFactory.getLogger(DaonAnalyzer.class);
	
	private Dictionary dictionary;
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
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
		
		//결과
		ResultTerms results = new ResultTerms(lookupResults);
		
		//
		for(int idx=0; idx<textLength;){
			
			//idx에 해당하는 기분석 사전 결과 가져오기
			List<Term> currentTerms = lookupResults.get(idx);
			
//			System.out.println(idx + " - " + texts[idx] + " : " + currentTerms);

			
			/**
			 * 품사 결합 조건 체크
			 */
			//이전 추출 term 가져오기 
			Term prevTerm = results.getPrevTerm();
			
			//이전 추출 term과 현재 추추 term이 존재
			if(prevTerm != null && currentTerms != null){
				//이전 추출 term 이 체언(명사)인 경우 뒤에 조사/어미 여부를 체크
				if(isNoun(prevTerm)){
					
					Term t = firstTerm(currentTerms);
					if(isJosa(t)){

						results.add(t);
						
						idx += t.getLength();
						
						continue;
					}
				}
				
				//용언(동사) 인 경우 어미 여부 체크 ?
//				if(isVerb(currentTerm)){
//					
//					Term t = firstTerm(terms);
//					if(isEomi(t)){
//
//						result.add(t);
//						idx++;
//						
//						continue;
//					}
//				}
			}
			
			
			
			//현재 기분석 term이 존재하는 경우
			if(currentTerms != null){
				//좌 -> 우 최장일치법 or 최적 수치 사용?
				//마지막이 제일 긴 term 
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
			}
			
//			System.out.println(texts[idx]);
		}
		
		
//		System.out.println("################ results #################");
//		for(Term t : results.getResults()){
//			System.out.println(t);
//		}
		
		return results;
	}
	


	
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
		
		while(endIdx < textLength){
			endIdx++;
			List<Term> terms = lookupResults.get(endIdx);
			
			if(terms != null){
				break;
			}
		}

		int length = (endIdx - startIdx);
		
		String unkownWord = new String(texts, startIdx, length);
		
		Keyword word = new Keyword(unkownWord, "UNKNOWN");
		Term unknowTerm = new Term(word, startIdx, length);
		
		return unknowTerm;
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

	private boolean isNoun(Term t){
		
		return isStartWith(t,  "N");
	}

	private boolean isJosa(Term t){
		
		return isStartWith(t,  "J");
	}


	private boolean isVerb(Term t){
		
		return isStartWith(t,  "V");
	}

	private boolean isEomi(Term t){
		
		return isStartWith(t,  "E");
	}
	
	private boolean isStartWith(Term t, String startWith){
		boolean is = false;
		
		if(t != null && t.getKeyword() != null){
			
			for(String attr : t.getKeyword().getAttr()){
				if(attr.startsWith(startWith)){
					return true;
				}
			}
		}
		
		return is;
	}
}
