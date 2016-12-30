package daon.analysis.ko.tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tag {
	
	private Logger logger = LoggerFactory.getLogger(Tag.class);

	private Map<String,Long> tagBits = new HashMap<String,Long>();
	
	public Tag(Map<String,Long> tagBits) {
		
		this.tagBits = tagBits;
	}
	
}
