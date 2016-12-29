package daon.analysis.ko.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import daon.analysis.ko.dict.config.Config.POSTag;

public class TagConnection {
	
	/**
	 * 현재 태그 
	 */
	private String tag;
	
	/**
	 * 다음 매칭 가능 태그들
	 */
	private List<String> tags;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "TagConnection [tag=" + tag + ", tags=" + tags + "]";
	}
}
