package daon.analysis.ko;

import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import daon.analysis.ko.model.Keyword;

public class TestTermLoad {
	
	@Test
	public void loadJson() throws JsonParseException, JsonMappingException, IOException{
		String json = "{\"seq\":1,\"word\":\"\",\"tag\":\"sc\",\"irrRule\":null,\"tf\":0,\"subWords\":null}";
		ObjectMapper mapper = new ObjectMapper();
		
		long start = System.currentTimeMillis();
		
		for(int i=0;i<1000000; i++){
			Keyword term = mapper.readValue(json, Keyword.class);
		}
		
		long end = System.currentTimeMillis();
		
		
		System.out.println("json : " + (end - start) + "ms");
		
	}
	
	
	@Test
	public void loadText(){
		String text = "나이키|nc|100";
		
		long start = System.currentTimeMillis();
		
		for(int i=0;i<1000000; i++){
			String[] data = text.split("[|]");
			Keyword term = new Keyword(data[0], data[1]);
			term.setWord(data[0]);
//			term.setAttr(data[1]);
			term.setTf(NumberUtils.toLong(data[2]));
		}
		
		long end = System.currentTimeMillis();
		
		
		System.out.println("text : " + (end - start) + "ms");
	}
	
	@Test
	public void testLong(){
//		long creditCardNumber = 12345678901234567890L;
		
//		System.out.println(creditCardNumber);
		
	}
	
}
