package daon.analysis.ko;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import daon.analysis.ko.dict.config.Config.DicType;
import daon.analysis.ko.dict.test.Dictionary;
import daon.analysis.ko.dict.test.DictionaryBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDictionary {
	private static String encoding = Charset.defaultCharset().name();

	private static Dictionary kkmDic;
	private static Dictionary nounDic;
	
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

		loadTestCase("testcase.txt");
		
		loadDictionary();
	}
	
	private static void loadDictionary() throws Exception {
		// https://lucene.apache.org/core/6_0_0/core/org/apache/lucene/util/fst/package-summary.html
		
		kkmDic = DictionaryBuilder.create().setDicType(DicType.KKM).setFileName("kkm.dic").setReader(new FileDictionaryReader<Keyword>()).build();
//		nounDic = DictionaryBuilder.create().setDicType(DicType.N).setFileName("noun.dic").setReader(new FileDictionaryReader<Word>()).build();
//		verbDic = DictionaryBuilder.create().setDicType(DicType.V).setFileName("verb.dic").setReader(new FileDictionaryReader<Word>()).build();
//		adverbDic = DictionaryBuilder.create().setDicType(DicType.M).setFileName("adverb.dic").setReader(new FileDictionaryReader<Word>()).build();
//		josaDic = DictionaryBuilder.create().setDicType(DicType.J).setFileName("josa.dic").setReader(new FileDictionaryReader<Word>()).build();
//		eomiDic = DictionaryBuilder.create().setDicType(DicType.E).setFileName("eomi.dic").setReader(new FileDictionaryReader<Word>()).build();
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

//		String text = "k2등산화 나이키k5 audi사나이 신발";
		String text = "사람이사랑을할때밥먹어야지";
//		String text = "전세계 abc최고가";
		
		//띄어쓰기 단위 분리
		
		//사전 추출 단어
		
		//숫자, 영문, 특수기호 분리
		char[] texts = text.toCharArray();
		
		int textLength = text.length();

		List<Term> results = kkmDic.lookup(texts, 0, textLength);

		System.out.println("########### kkm ###############");
		
//		우리말은 제2 유형인 SOV에 속한다. 따라서 국어의 기본 어순은 ‘주어+목적어+서술어’이다.
		// 1순위) NN 맨 앞 + NN + (J) + V + (E)
		// 2순위) M 맨 앞 + V + (E)
		// 3순위) V 맨 앞
		
		List<Term> pending = new ArrayList<>();
		List<Term> unkownPending = new ArrayList<>();
		
		//마지막 end값
		int end = 0;
				
//		Map<TermKey,Term> longestTerms = new HashMap<TermKey,Term>();
		
		for(int i=0, len=results.size(); i<len; i++){
			Term t = results.get(i);

//			System.out.println(t);
			
			int offset = t.getOffset();
			int length = t.getLength();
			
			//현재 end값
			int termEnd = offset + length;

			//중간 사전 없는 문자 가져오기. 공백 제거?
			if(offset > end){
				String unkownWord = text.substring(end, offset);
				System.out.println("unkownWord=" + unkownWord);
				
				Keyword word = new Keyword(unkownWord, "UNKOWN");
				unkownPending.add(new Term(word, end, offset));
			}
			
			//마지막 end값 설정
			if(end < termEnd){
				end = termEnd;
			}
			
//			System.out.println("currentTerm=" + t);
			int size = pending.size();
			
			if(size > 0){
				Term before = pending.get(size - 1);
				
				System.out.println("before : " + before.getKeyword().getWord() + ", current : " + t.getKeyword().getWord() + ", before.isGreaterThan(t) : " + before.isGreaterThan(t) + ", t.isGreaterThan(before) : " + t.isGreaterThan(before));
				
				//최장일치 
				if(!before.isGreaterThan(t)){
					if(!t.equals(before)){
						
						// 1순위) NN 맨 앞 + NN + (J) + V + (E)
						// 2순위) M 맨 앞 + V + (E)
						// 3순위) V 맨 앞
						if(isNoun(before) && isJosa(t)){
							pending.add(t);
						}else{
						
							Term longestTerm = getLongestTerm(t);
							
	//						System.out.println("longestTerm=" + longestTerm);
				
							pending.add(longestTerm);
						}
					}
				}
			}else{
				Term longestTerm = getLongestTerm(t);
	
//				System.out.println("longestTerm=" + longestTerm);
	
				pending.add(longestTerm);
			}
			
		}
		
		System.out.println("#####################");
		
//		for(Entry<TermKey,Term> entry : longestTerms.entrySet()){
//			System.out.println(entry.getKey() + "-" + entry.getValue());
//		}

		System.out.println("#####################");
		
		for(Term t : pending){
			System.out.println(t);
		}
		
//		System.out.println(pending);
		
	}
	
	private boolean isNoun(Term t){
		
		return isStartWith(t,  "N");
	}

	private boolean isJosa(Term t){
		
		return isStartWith(t,  "J");
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
	
	
	
	private Term getLongestTerm(Term t) {
		Term next = t.getNextTerm();
		Term prev = t.getPrevTerm();
		
		//맨처음
		if(next != null && prev == null){
			
			if(next.isGreaterThan(t)){
				return getLongestTerm(next);
			}
		}
		
		//마지막 
		if(prev != null && next == null){
			
			if(prev.isGreaterThan(t)){
				return getLongestTerm(prev);
			}
		}
		
		if(prev != null && next != null){
			
			if(next.isGreaterThan(t)){
				if(prev.isGreaterThan(next)){
					return getLongestTerm(prev);
				}else{
					return getLongestTerm(next);
				}
			}
			

			if(prev.isGreaterThan(t)){
				if(next.isGreaterThan(prev)){
					return getLongestTerm(next);
				}else{
					return getLongestTerm(prev);
				}
			}
		}
		
		return t;
	}

	@Ignore
	@Test 
	public void bAnalyzeTestcase() throws IOException{

		
//		char[] text = "k2등산화 나이키사나이신발".toCharArray();
//		
//		List<Term> results = lookup(text,0,text.length);
//		
//		for(Term t : results){
//			System.out.println(t);
//		}
		
		long totalElapsed = 0;
		
		for(int i=0; i<10; i++){
			long start = System.currentTimeMillis();
			
			for(String keyword : keywords){
			
				char[] text = keyword.toCharArray();
				
				List<Term> results1 = kkmDic.lookup(text,0,text.length);
				
	
	//			List<Term> results2 = dictionary.lookup(text,0,text.length);
	//
	//			List<Term> results3 = dictionary.lookup(text,0,text.length);
	//
	//			List<Term> results4 = dictionary.lookup(text,0,text.length);
	//
	//			List<Term> results5 = dictionary.lookup(text,0,text.length);
				
	//			System.out.print( keyword + " => ");
				for(Term t : results1){
	//				System.out.print(t.getWord().getWord() + ", ");
				}
			
	//			System.out.println();
			}
			
			long end = System.currentTimeMillis();
			
			totalElapsed += (end - start);
			
			System.out.println("analyze elapsed : " + (end - start) + "ms");
		}
		
		System.out.println("totalElapsed elapsed : " + totalElapsed + "ms");
//		try {
//			Thread.sleep(1800000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
}
