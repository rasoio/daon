package daon.analysis.ko;

import java.io.IOException;

import daon.analysis.ko.reader.JsonFileReader;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import daon.analysis.ko.dict.BaseDictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.model.Keyword;

public class TestDataLoad {

    private Logger logger = LoggerFactory.getLogger(TestDataLoad.class);

    @Test
    public void loadProbability() throws InterruptedException {

        int length = 10070045;

//        Thread.sleep(30000);

        System.out.println("Start!!");

        Probability[] probabilities = new Probability[length];

        for(int i=0; i< length; i++){
            long a = RandomUtils.nextLong(0, length);
            long b = RandomUtils.nextLong(0, length);
            long c = RandomUtils.nextLong(0, length);
            long d = RandomUtils.nextLong(0, length);


            probabilities[i] = new Probability(a,b,c,d);
        }

        System.out.println("End!!");

        Thread.sleep(100000);

    }


    public class Probability{

        private long pOuterWordSeq;
        private long pInnerWordSeq;
        private long nInnerWordSeq;
        private long nOuterWordSeq;

        public Probability(long pOuterWordSeq, long pInnerWordSeq, long nInnerWordSeq, long nOuterWordSeq) {
            this.pOuterWordSeq = pOuterWordSeq;
            this.pInnerWordSeq = pInnerWordSeq;
            this.nInnerWordSeq = nInnerWordSeq;
            this.nOuterWordSeq = nOuterWordSeq;
        }

        public long getpOuterWordSeq() {
            return pOuterWordSeq;
        }

        public void setpOuterWordSeq(long pOuterWordSeq) {
            this.pOuterWordSeq = pOuterWordSeq;
        }

        public long getpInnerWordSeq() {
            return pInnerWordSeq;
        }

        public void setpInnerWordSeq(long pInnerWordSeq) {
            this.pInnerWordSeq = pInnerWordSeq;
        }

        public long getnInnerWordSeq() {
            return nInnerWordSeq;
        }

        public void setnInnerWordSeq(long nInnerWordSeq) {
            this.nInnerWordSeq = nInnerWordSeq;
        }

        public long getnOuterWordSeq() {
            return nOuterWordSeq;
        }

        public void setnOuterWordSeq(long nOuterWordSeq) {
            this.nOuterWordSeq = nOuterWordSeq;
        }
    }


    @Test
    public void load() throws JsonParseException, JsonMappingException, IOException, InterruptedException {

//        BaseDictionary dic = (BaseDictionary) DictionaryBuilder.create().setFileName("rouzenta_trans.dic").setReader(new JsonFileReader<Keyword>()).setValueType(Keyword.class).build();

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
