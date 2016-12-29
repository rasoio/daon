package daon.analysis.ko.tag;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tag {
	
	private Logger logger = LoggerFactory.getLogger(Tag.class);

	private Set<String> tagSet;
	
	public Tag(Set<String> tagSet) {
		
		this.tagSet = tagSet;
	}

	
	public boolean isValid(String tags){
		return tagSet.contains(tags);
	}
	

	
	
}
