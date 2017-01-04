package daon.analysis.ko;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TestWordCount {
	
	private Logger logger = LoggerFactory.getLogger(TestWordCount.class);

	@Ignore
	@Test
	public void load() throws JsonParseException, JsonMappingException, IOException, InterruptedException{
		
		File csv = new File("/Users/mac/Downloads/tf.csv");
		
		File f = new File("/Users/mac/Downloads/sejong.pos");
		
		String txt = FileUtils.readFileToString(f, "UTF-8");
		
		FileUtils.write(csv, "", "UTF-8");
		
		final InputStream in = TestSingleWordPhrase.class.getResourceAsStream("dict/reader/rouzenta.dic");
    	List<String> lines = IOUtils.readLines(in, Charset.defaultCharset());

    	
    	List<String> rawWords = new ArrayList<String>();
    	
    	String[] words = txt.split("\\s+");
    	
    	for(String word : words){
    		
    		String[] ws = word.split("[+]");

    		for(String w : ws){
    			rawWords.add(w);
    		}
    	}
    	
    	Map<String, Long> wordTf = rawWords.stream().collect(Collectors.groupingBy( w -> w.toString(),
    			Collectors.counting()
          ));
    	
		
    	for(String line : lines){
    		if(line.startsWith("!") || StringUtils.isEmpty(line)){
    			continue;
    		}
    		
    		if(line.startsWith("LEXICON") || line.startsWith(" ") || StringUtils.isBlank(line)){
    			continue;
    		}else{
    			
    			String[] dic = line.split("\\s+");
    			String word = dic[0];
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
	
}
