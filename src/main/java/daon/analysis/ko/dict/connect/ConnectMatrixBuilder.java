package daon.analysis.ko.dict.connect;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.reader.Reader;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.TagInfo;

public class ConnectMatrixBuilder {

	private Logger logger = LoggerFactory.getLogger(ConnectMatrixBuilder.class);

	private Config config = new Config();
	private Reader<TagConnection> reader;
	
	public static ConnectMatrixBuilder create() {
		return new ConnectMatrixBuilder();
	}

	private ConnectMatrixBuilder() {}

	public final ConnectMatrixBuilder setFileName(final String fileName) {
		this.config.define(Config.FILE_NAME, fileName);
		return this;
	}
	
	public final ConnectMatrixBuilder setReader(final Reader<TagConnection> reader) {
		this.reader = reader;
		return this;
	}
	
	public final ConnectMatrixBuilder setValueType(final Class<TagConnection> valueType) {
		this.config.define(Config.VALUE_TYPE, valueType);
		return this;
	}
	
	public ConnectMatrix build() throws IOException{
		
		if(reader == null){
			//TODO throw exception 
		}
		
		try{
			reader.read(config);
			
			Map<String,Long> tagBits = new HashMap<String,Long>();
			
			Map<String,Float> tagProb = new HashMap<String,Float>();
			
//			logger.info("reader read complete");
			while (reader.hasNext()) {
				TagConnection tag = reader.next();

				String mainTagName = tag.getTag();
				long bits = 0l;
				
				for(TagInfo subTag : tag.getTags()){
					
					POSTag tagType = subTag.getTag();
					bits |= tagType.getBit();
					
					String subTagName = tagType.name();
					
					if("Root".equals(mainTagName)){
						tagProb.put(subTagName, subTag.getProb());	
					}else{
						String key = mainTagName + "|" + subTagName;

						tagProb.put(key, subTag.getProb());	
					}
				}
				
				tagBits.put(mainTagName, bits);
				
//				tags.add(tag);
//				logger.info("tag => {}", tag);
			}

			/*
			tags.stream().collect(Collectors.groupingBy(
					Function.identity(), 
	                Collectors.counting()
	          )).entrySet().stream().filter(e -> e.getValue() == 1).forEach(e -> { System.out.println(e);});;
	         */
			
			return new ConnectMatrix(tagProb);
		} finally {
			reader.close();
		}
	}
}
