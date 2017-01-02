package daon.analysis.ko.tag;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.util.Utils;

public class Tag {
	
	private Logger logger = LoggerFactory.getLogger(Tag.class);

	private Map<String,Long> tagBits = new HashMap<String,Long>();
	
	public Tag(Map<String,Long> tagBits) {
		this.tagBits = tagBits;
	}
	
	public boolean isValid(String tagKey, POSTag tag){
		boolean isValid = false;
		
		Long bits = tagBits.get(tagKey);
		
		
		if(bits == null){
			return isValid;
		}
		
		long result = bits & tag.getBit();
		
		if(result > 0){
			isValid = true;
		}
		
		return isValid;
	}
}
