package daon.analysis.ko;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.BaseDictionary;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.config.Config.DicType;
import daon.analysis.ko.dict.reader.FileDictionaryReader;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDictionary {
	
	private Logger logger = LoggerFactory.getLogger(TestDictionary.class);
	
	private static String encoding = Charset.defaultCharset().name();

	private static Dictionary kkmDic;
	
	private static List<String> keywords; 
	
	public static void loadTestCase(String fileName) throws Exception{

		final InputStream in = TestDictionary.class.getResourceAsStream(fileName);
		
		try {
			keywords = IOUtils.readLines(in, Charsets.toCharset(encoding));
		} finally {
			IOUtils.closeQuietly(in);
		}
		
	}
	
	@BeforeClass
	public static void load() throws Exception{

		//테스트 케이스 파일 로딩
		loadTestCase("testcase.txt");
		
		//기분석 사전 로딩
		loadDictionary();
	}
	
	private static void loadDictionary() throws Exception {
		// https://lucene.apache.org/core/6_0_0/core/org/apache/lucene/util/fst/package-summary.html
		
		kkmDic = DictionaryBuilder.create().setDicType(DicType.KKM).setFileName("kkm.dic").setReader(new FileDictionaryReader()).build();
	}
	
	@Ignore
	@Test
	public void convert(){
		char c = 566;
		
		char[] buffer = new char[] {'ㄱ','ㅎ','ㅏ','ㅣ','ㅣ','가','힣','	','나','이','뻏','쁔'};
		
        int leng = buffer.length;
        for(int i=0;i<leng;i++) {
        	
        	String type = "";
            if(buffer[i]=='\u0000') type="EMPTY";
            
//            if(buffer[i]>=0x1100 && buffer[i]<=0x11FF) type="KOREAN JAMO"; // 12592, 12687 = 95
            if(buffer[i]>='\u3130' && buffer[i]<='\u318F') type="KOREAN JAMO"; // 12592, 12687 = 95
//            if(buffer[i]>='\uAC00' && buffer[i]<='\uD7A3') type="KOREAN"; // 44032, 55203 = 11171
            if(buffer[i]>=0xAC00 && buffer[i]<=0xD7A3) type="KOREAN"; // 44032, 55203 = 11171
            if(buffer[i]>='\u0000' && buffer[i]<='\u007F') type="NUM_ENG_ETC"; // 0, 127 = 127

        	System.out.println(buffer[i] + ", type=" + type);
        }
		
		System.out.println("'" + '\u0000' + "'"); //빈문자 
		System.out.println("'" + '\u0061' + "'"); //a
		
		
	}
	
//	@Ignore
	@Test 
	public void cAnalyzeKeywordTest() throws IOException{

		List<String> exampleTexts = new ArrayList<String>();
		
		exampleTexts.add("k2등산화 나이키k5 audi사나이 신발");
		exampleTexts.add("123,445원");
		exampleTexts.add("아버지가방에들어가신다");
		exampleTexts.add("위메프 알프렌즈 신상반팔티");
		exampleTexts.add("k2등산화나나이키신발");
		exampleTexts.add("k2여행자는자고로밤에자야");
		exampleTexts.add("형태소 분석기의 적용 분야에 따라 공백이 포함된 고유명사");
		exampleTexts.add("사람이사랑을할때밥먹어야지");
		exampleTexts.add("전세계 abc최고가");
		for(String text : exampleTexts){
			//띄어쓰기 단위 분리
			
			//사전 추출 단어
			
			//숫자, 영문, 특수기호 분리
			
			//원본 문자
			char[] texts = text.toCharArray();
			
			//총 길이
			int textLength = text.length();
	
			//기분석 사전 매칭 정보 가져오기
			Map<Integer, List<Term>> lookupResults = kkmDic.lookup(texts, 0, textLength);
			
			logger.info("text : {}", text);
			
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
				
//				System.out.println(idx + " - " + texts[idx] + " : " + currentTerms);
	
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
			
			
			System.out.println("################ results #################");
			for(Term t : results.getResults()){
				System.out.println(t);
			}
		
		
		}
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
