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
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ResultTerms;
import daon.analysis.ko.model.Term;

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
	public void analyzeKeywordTest() throws IOException{

		List<String> exampleTexts = new ArrayList<String>();
		
		exampleTexts.add("그러자 그는 내게 진러미의 규칙을 가르쳐주었다.");
		exampleTexts.add("그러자그는내게진러미의규칙을가르쳐주었다.");
		exampleTexts.add("k2등산화 나이키k5 audi사나이 신발");
		exampleTexts.add("123,445원");
		exampleTexts.add("아버지가방에들어가신다");
		exampleTexts.add("위메프 알프렌즈 신상반팔티");
		exampleTexts.add("k2등산화나나이키신발");
		exampleTexts.add("k2여행자는자고로밤에자야");
		exampleTexts.add("형태소 분석기의 적용 분야에 따라 공백이 포함된 고유명사");
		exampleTexts.add("사람이사랑을할때밥먹어야지");
		exampleTexts.add("전세계 abc최고가");
		
		DaonAnalyzer analyzer = new DaonAnalyzer();
		analyzer.setDictionary(kkmDic);
		
		for(String text : exampleTexts){
			ResultTerms results = analyzer.analyze(text);
			
			System.out.println("################ results #################");
			for(Term t : results.getResults()){
				System.out.println(t);
			}
		
		
		}
		
//		try{
//			Thread.sleep(1000000000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally{
//			
//		}
	}
}
