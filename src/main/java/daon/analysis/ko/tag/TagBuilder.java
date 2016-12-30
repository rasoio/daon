package daon.analysis.ko.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.reader.Reader;
import daon.analysis.ko.model.TagConnection;

public class TagBuilder {

	private Logger logger = LoggerFactory.getLogger(TagBuilder.class);

	private Config config = new Config();
	private Reader<TagConnection> reader;
	
	public static TagBuilder create() {
		return new TagBuilder();
	}

	private TagBuilder() {}

	public final TagBuilder setFileName(final String fileName) {
		this.config.define(Config.FILE_NAME, fileName);
		return this;
	}
	
	public final TagBuilder setReader(final Reader<TagConnection> reader) {
		this.reader = reader;
		return this;
	}
	
	public final TagBuilder setValueType(final Class<TagConnection> valueType) {
		this.config.define(Config.VALUE_TYPE, valueType);
		return this;
	}
	
	public Tag build() throws IOException{
		
		if(reader == null){
			//TODO throw exception 
		}
		
		try{
			reader.read(config);
			
			Map<String,Long> tagBits = new HashMap<String,Long>();
			
//			logger.info("reader read complete");
			while (reader.hasNext()) {
				TagConnection tag = reader.next();

				String ts = tag.getTag();
				long bits = 0l;
				
				for(String t : tag.getTags()){
					
					POSTag tagType = POSTag.valueOf(t);
					bits |= tagType.getBit();
				}
				
				tagBits.put(ts, bits);
				
//				tags.add(tag);
//				logger.info("tag => {}", tag);
			}

			/*
			tags.stream().collect(Collectors.groupingBy(
					Function.identity(), 
	                Collectors.counting()
	          )).entrySet().stream().filter(e -> e.getValue() == 1).forEach(e -> { System.out.println(e);});;
	         */
			
			return new Tag(tagBits);
		} finally {
			reader.close();
		}
	}
}
