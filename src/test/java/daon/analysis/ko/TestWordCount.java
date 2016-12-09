package daon.analysis.ko;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TestWordCount {
	
	private Logger logger = LoggerFactory.getLogger(TestWordCount.class);
	
	@Test
	public void load() throws JsonParseException, JsonMappingException, IOException, InterruptedException{
		
		File csv = new File("/Users/mac/Downloads/tf.csv");
		
		File f = new File("/Users/mac/Downloads/sejong.pos");
		
		String txt = FileUtils.readFileToString(f, "UTF-8");
		
		FileUtils.write(csv, "", "UTF-8");
		
		final InputStream in = TestSingleWordPhrase.class.getResourceAsStream("dict/reader/rouzenta.dic");
    	List<String> lines = IOUtils.readLines(in, Charset.defaultCharset());
    	
    	String tag = "";
    	for(String line : lines){
    		if(line.startsWith("!") || StringUtils.isEmpty(line)){
    			continue;
    		}
    		
    		if(line.startsWith("LEXICON") || line.startsWith(" ") || StringUtils.isBlank(line)){
    			continue;
    		}else{
    			
    			String[] dic = line.split("[ ]+");
    			String word = dic[0];
    			String keyword = replaceWord(word);

    			int cnt = StringUtils.countMatches(txt, keyword);

//    			System.out.println(word + "	" + keyword + "	" + cnt);
    			
    			FileUtils.write(csv, word + "	" + keyword + "	" + cnt + System.lineSeparator(), "UTF-8", true);
    		}
    		
    		
    	}
		
		
		
		
	}

	private String replaceWord(String string) {
		
		String word = string.replaceAll("%_", "");
		word = word.replaceAll("([/][a-z][a-z])([ㄱ-힣]+)", "$1+$2");
		
		if(word.startsWith("%")){
			word = word.replaceFirst("%", "");
		}
		
		return word;
	}
	
}
