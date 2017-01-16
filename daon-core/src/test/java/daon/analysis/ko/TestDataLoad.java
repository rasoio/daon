package daon.analysis.ko;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import daon.analysis.ko.dict.BaseDictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;

public class TestDataLoad {

    private Logger logger = LoggerFactory.getLogger(TestDataLoad.class);

    @Test
    public void load() throws JsonParseException, JsonMappingException, IOException, InterruptedException {

        BaseDictionary dic = (BaseDictionary) DictionaryBuilder.create().setFileName("rouzenta_trans.dic").setReader(new FileReader<Keyword>()).setValueType(Keyword.class).build();

//		Map<String,List<Long[]>> datas = dic.getData();

//		System.out.println(datas.size());

//		for(int i=0;i<3;i++){
//			for(Map.Entry<String,List<Keyword>> e : datas.entrySet()){
//				
//				String key = e.getKey();
//				
//				List<Keyword> k = e.getValue();
//				
//				
//				datas.put(key + i, k);
//			}
//		}

        System.out.println("complete!!");

//		200만건 200MB, 500만건 500MB 정도...

//		Thread.sleep(100000l);

    }

}
