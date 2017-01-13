package daon.analysis.ko.main;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.dict.reader.Reader;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.TagCost;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MakeProbability {
	
	private Logger logger = LoggerFactory.getLogger(MakeProbability.class);
	
	public static ObjectMapper om = new ObjectMapper();

	public void load() throws JsonParseException, JsonMappingException, IOException, InterruptedException{
		
		File csv = new File("/Users/mac/Downloads/tf.csv");
		
		File f = new File("/Users/mac/Downloads/sejong.pos");
		
		String txt = FileUtils.readFileToString(f, "UTF-8");

        File wfile = new File("/Users/mac/git/daon/src/test/resources/daon/analysis/ko/dict/reader/connect_matrix2.dic");
        
        File rouzenta = new File("/Users/mac/git/daon/src/test/resources/daon/analysis/ko/dict/reader/rouzenta.dic");
        FileInputStream in = new FileInputStream(rouzenta);

    	List<String> lines = IOUtils.readLines(in, Charset.defaultCharset());

    	
    	List<String> rawWords = new ArrayList<String>();
    	
    	String[] words = txt.split("\\s+");
    	
    	/*
    	 - 구해야 되는 값
    	 1. tag 별 집계(tagfreq), tag 연결 집계(tranfreq) 구하기.. (앞 태그 + 뒤 태크)
    	 2. 단어 집계(word), 단어+태그 집계(dic) 구하기..
    	 3. 총 단어수(totnwords). 총 tag 수(totntags) 구하기
    	 
    	 - 사용할 값 
    	 1. 품사에서 어휘가 발생할 확률 => V
    	    freq = 한 단어+태그 freq 수 ( default : 0.5 ) 예) 학교|nc의 freq
        	prob   = - log ( float (freq) / float (totnwords) )
        	
         2. 품사 천이 확률 	
         	?? 아마도 아래와 같을것
         	freq = 한 tag 연결 집계(tranfreq) ( default : ?? ) 예) nc|nc의 freq
         	prob   = - log ( float (freq) / float (totntags) )
         	
         => 전부의 (곱의 합) OR (합) ? 이 가장 큰것을 사용
         
         prob => freq 작을 수록 큰수가 됨. 크면 작은수 
         
         minimum distance algorithm (log의 합이 최소가 되는 path)을 수행한 결과물
         
         sejong.pos 와 같은 파일(형태소 정의 파일)을 가지고 사전 데이터와 조합을 쉽게 사용할수 있게 새로 만들어야 할듯..
         아니면 새로운 사전 단어를 만들수 있게 하던지..  
    	 */
    	
    	 
//    	22634887 총 단어 출현 수     	
//    	22231026 사전에 있는 단어 출현 수 
//    	.......................................................................................Total Words = 22231026, totntags = 22634340
//    			나 freq = 106061   나/np freq = 56108
//    			# of used symbols : 1335636
//    			# of Unique Symbols : 7214
    			
    	int totalWordCnt = 0;
    	
//    	def InitTagFreq():
//    	    tagfreq  = {}
//    	    tranfreq = {}
//    	    for tag in basictags :
//    	        tagfreq[tag] = 0
//    	        tranfreq[tag] = {}
//    	        for totag in basictags :
//    	            tranfreq[tag][totag] = 0
//    	    return (tagfreq, tranfreq)
    	
//    	tagfreq[tag]        = tagfreq[tag] + 1
//      tranfreq[ptag][tag] = tranfreq[ptag][tag] + 1
    	
    	List<String> tags = new ArrayList<String>();
    	List<String> trans = new ArrayList<String>();
    	
    	for(String word : words){
    		
    		String[] ws = word.split("[+]");

    		String ptag = null; //"ini";
    		for(String w : ws){
    			rawWords.add(w);
    			
    			String[] data = w.split("/");
    			
    			if(data.length < 2){
//    				System.out.println("'" + data[0] + "'");
    			}else{
	    			String tag = data[1];
	    			
	    			if(StringUtils.isNotEmpty(tag)){
	    			
		    			tags.add(tag);
		    			
		    			if(ptag != null){
		    				String comb = ptag + "|" + tag;
		    				
		    				trans.add(comb);
		    			}
		    			
		    			totalWordCnt++;
		    			ptag = tag;
	    			}
    			}
    		}
    	}
    	
    	Map<String, Long> tagFreq = tags.stream().collect(Collectors.groupingBy( Function.identity(),
    			Collectors.counting()
          ));
    	
    	
    	final int totntags = totalWordCnt;
    	
    	Map<String, Long> tranfreq = trans.stream().collect(Collectors.groupingBy( Function.identity(),
    			Collectors.counting()
          ));

    	System.out.println("totalWordCnt : " + totalWordCnt);

    	FileUtils.write(wfile, "", "UTF-8", false);
    	
    	Config config = new Config();
    	config.define(Config.FILE_NAME, "connect_matrix.dic");
    	config.define(Config.VALUE_TYPE, TagConnection.class);
    	
    	Reader<TagConnection> reader = new FileReader<TagConnection>();
    	reader.read(config);
    	
    	while (reader.hasNext()) {
    		
    		TagConnection newTag = new TagConnection();
    		
			TagConnection tag = reader.next();
			
			String tagKey = tag.getTag();
			
			newTag.setTag(tagKey);
			
			List<TagCost> newSubTags = new ArrayList<TagCost>();

			List<TagCost> subTags = tag.getTags();
			
			for(TagCost subTag : subTags){
				String tagName = subTag.getTag().name();
				String key = tagName;
				Long cnt = 0l;
				
				
				if(tagName.length() == 4){
					//앞 품사 사용
					key = tagName.substring(0,2);
				}
				
				if("Root".equals(tagKey)){
					
					cnt = tagFreq.get(key);
					
				}else if("fin".equals(subTag)){
					
				}else{
					String transKey = "";
					
					//메인 태그는 뒤 품사 사용 
					if(tagKey.length() == 4){
						transKey = tagKey.substring(2) + "|";
					}else{
						transKey = tagKey + "|";
					}
					
					transKey += key;
					
					cnt = tranfreq.get(transKey);
					
				}
				
				float prob = 0;
				
				if(cnt != 0){
					prob = (float) (-Math.log( (float) (cnt) / (float) (totntags)));
				}
				
				TagCost info = new TagCost(tagName, cnt, prob);
				
				newSubTags.add(info);
			}
			
			newTag.setTags(newSubTags);
			
			String line = om.writeValueAsString(newTag);
			
			System.out.println(line);
			
			FileUtils.write(wfile, line + System.lineSeparator(), "UTF-8", true);
			
    	}
    	
    	
    	/*
    	Map<String, Long> wordTf = rawWords.stream().collect(Collectors.groupingBy( w -> w.toString(),
    			Collectors.counting()
          ));
    	
		FileUtils.write(csv, "", "UTF-8");
		
    	for(String line : lines){
    		if(line.startsWith("!") || StringUtils.isEmpty(line)){
    			continue;
    		}
    		
    		if(line.startsWith("LEXICON") || line.startsWith(" ") || StringUtils.isBlank(line)){
    			continue;
    		}else{
    			
    			String[] dic = line.split("\\s+");
    			
    			//원본 사전 키워드
    			String word = dic[0];
    			
    			//불필요 정보 제거한 키워드 = sejong.pos 에 정의 된 키워드 
    			String keyword = replaceWord(word);

    			int cnt = 0;
    			
    			Long tf = wordTf.get(keyword);
    			
    			if(tf == null){
    				cnt = StringUtils.countMatches(txt, keyword);
    			}else{
    				cnt = tf.intValue();
    			}

//    			System.out.println(word + "	" + keyword + "	" + cnt);
    			
    			FileUtils.write(csv, word + "	" + keyword + "	" + cnt + System.lineSeparator(), "UTF-8", true);
    		}
    		
    	}
    	*/
		
	}

	private String replaceWord(String string) {
		
		String word = string.replaceAll("%_", "");
		word = word.replaceAll("/irr.", "");
		word = word.replaceAll("([/][a-z][a-z])([ㄱ-힣]+)", "$1+$2");
		
		if(word.startsWith("%")){
			word = word.replaceFirst("%", "");
		}
		
		return word;
	}

    public static void main(String[] args) throws IOException, InterruptedException {
        MakeProbability makeTF = new MakeProbability();
        makeTF.load();

    }
}
