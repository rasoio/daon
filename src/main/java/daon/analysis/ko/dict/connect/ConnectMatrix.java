package daon.analysis.ko.dict.connect;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config.POSTag;

public class ConnectMatrix {
	
	private Logger logger = LoggerFactory.getLogger(ConnectMatrix.class);

//	private Map<String,Long> tagBits = new HashMap<String,Long>();
	private Map<String,Float> tagProb = new HashMap<String,Float>();
	
	public ConnectMatrix(Map<String,Float> tagProb) {
		this.tagProb = tagProb;
	}
	
	public float score(POSTag prevTag, POSTag curTag){
		float score = 100;
		
		String key = prevTag + "|" + curTag;
		
		Float prob = tagProb.get(key);
		
		if(prob != null){
			score = prob;
		}
		
		return score;
	}
	
	
	public boolean isValid(String tagKey, POSTag tag){
		boolean isValid = false;
		
//		Long bits = tagBits.get(tagKey);
//		
//		
//		if(bits == null){
//			return isValid;
//		}
//		
//		long result = bits & tag.getBit();
//		
//		if(result > 0){
//			isValid = true;
//		}
		
		return isValid;
	}
}
