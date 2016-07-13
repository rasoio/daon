package daon.analysis.ko.dict.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.DictionaryReader;
import daon.analysis.ko.Word;
import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.DicType;

public class DictionaryBuilder {

	private Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);

	private Config config = new Config();
	private DictionaryReader<Word> reader;

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
	
	public final DictionaryBuilder setReader(final DictionaryReader<Word> reader) {
		this.reader = reader;
		return this;
	}
	
	public Dictionary build() throws IOException{
		
		if(reader == null){
			//TODO throw exception 
//			throw new 
		}
		
		reader.read(config);
		
		logger.info("reader read complete");
		
		StopWatch watch = new StopWatch();
		
		watch.start();
		
		List<Word> data = new ArrayList<Word>();
		
		PositiveIntOutputs fstOutput = PositiveIntOutputs.getSingleton();
		Builder<Long> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);
		IntsRefBuilder scratch = new IntsRefBuilder();
		long ord = 0;

		while (reader.hasNext()) {
			Word term = reader.next(Word.class);

			if(term == null){
				continue;
			}
			
			if(logger.isDebugEnabled()){
				logger.debug("term={}",term);
			}
			
			// add mapping to FST
			String word = term.getWord();
			scratch.grow(word.length());
			scratch.setLength(word.length());
			
			for (int i = 0; i < word.length(); i++) {
				scratch.setIntAt(i, (int) word.charAt(i));
			}
			
			fstBuilder.add(scratch.get(), ord);
			data.add(term);
			ord++;
		}
		
		TokenInfoFST fst = new TokenInfoFST(fstBuilder.finish());
		
		Word[] words = data.toArray(new Word[data.size()]);
		watch.stop();
		
		logger.info("fst load : {} ms", watch.getTime());
		
		return new BaseDictionary(fst, words);
	}
}
