package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.DicType;
import daon.analysis.ko.dict.reader.DictionaryReader;
import daon.analysis.ko.model.Keyword;

public class DictionaryBuilder {

	private Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);

	private Config config = new Config();
	private DictionaryReader reader;

	public static DictionaryBuilder create() {
		return new DictionaryBuilder();
	}

	private DictionaryBuilder() {
		super();
	}

	public final DictionaryBuilder setFileName(final String fileName) {
		this.config.define(Config.FILE_NAME, fileName);
		return this;
	}

	public final DictionaryBuilder setDicType(final DicType type) {
		this.config.define(Config.DICTIONARY_TYPE, type);
		return this;
	}
	
	public final DictionaryBuilder setReader(final DictionaryReader reader) {
		this.reader = reader;
		return this;
	}
	
	public Dictionary build() throws IOException{
		
		if(reader == null){
			//TODO throw exception 
		}
		
		try{
			reader.read(config);
			
			logger.info("reader read complete");
			
			StopWatch watch = new StopWatch();
			
			watch.start();
			
			//seq 별 Keyword
			Map<Long,Keyword> data = new HashMap<Long,Keyword>();
			
			PositiveIntOutputs fstOutput = PositiveIntOutputs.getSingleton();
			Builder<Long> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);
			IntsRefBuilder scratch = new IntsRefBuilder();
			
			while (reader.hasNext()) {
				Keyword keyword = reader.next();
	
				if(keyword == null){
					continue;
				}
				
				// add mapping to FST
				long seq = keyword.getSeq();
				String word = keyword.getWord();
				scratch.grow(word.length());
				scratch.setLength(word.length());
				
				for (int i = 0; i < word.length(); i++) {
					scratch.setIntAt(i, (int) word.charAt(i));
				}
				
				if(logger.isDebugEnabled()){
					logger.debug("seq={}, keyword={}, scratch={}",seq, keyword, scratch.get());
				}
				
				fstBuilder.add(scratch.get(), seq);
				
				data.put(seq, keyword);
			}
			
			TokenInfoFST fst = new TokenInfoFST(fstBuilder.finish());
			
			watch.stop();
			
			logger.info("fst load : {} ms", watch.getTime());
			
			return new BaseDictionary(fst, data);

		} finally {
			reader.close();
		}
	}
}
