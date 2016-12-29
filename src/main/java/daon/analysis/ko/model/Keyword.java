package daon.analysis.ko.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import daon.analysis.ko.dict.config.Config.POSTag;

public class Keyword {
	
	private Logger logger = LoggerFactory.getLogger(Keyword.class);

	/**
	 * 사전 단어 구분 키값
	 */
	private long seq;
	
	/**
	 * 사전 단어 
	 */
	private String word;
	
	/**
	 * 사전 단어 추가 정보 
	 * POS tag 정보 목록
	 */
	private String tag;
	
	/**
	 * 불규칙 룰
	 */
	private String irrRule;
	
	/**
	 * 사전 단어 사용 빈도
	 */
	private long tf;
	
	/**
	 * 품사 체크용 정보
	 */
	@JsonIgnore
	private long tagBit;
	
	/**
	 * 단어 하위 단어 정보 (복합명사인 경우)
	 */
	private List<Keyword> subWords;
	
	/**
	 * 단어 설명 
	 */
	private String desc = "";

	public Keyword(){}
	
	public Keyword(String word, String tag) {
		this.word = word;
		this.tag = tag;
		this.tf = 0;
		this.tagBit = toBit(tag);
	}
	
	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tagBit = toBit(tag);
		
		this.tag = tag;
	}
	
	public String getIrrRule() {
		return irrRule;
	}

	public void setIrrRule(String irrRule) {
		this.irrRule = irrRule;
	}

	/**
	 * attr 정보에 존재하는 품사 정보 조합
	 * @param attr
	 * @return
	 */
	private long toBit(String tag) {
		long bits = 0l;

		try{
			if(StringUtils.isNotBlank(tag)){
				//태그 조합인 경우 앞부분만 적용
				if(tag.length() == 4){
					String tag1 = tag.substring(0, 2);
//					String tag2 = tag.substring(2);
					
					POSTag tagType = POSTag.valueOf(tag1);
					bits |= tagType.getBit();

//					tagType = POSTag.valueOf(tag2);
//					bits |= tagType.getBit();
				}else{
					POSTag tagType = POSTag.valueOf(tag);
					
					bits |= tagType.getBit();
				}
			}
		}catch(IllegalArgumentException e){
			logger.error("['{}'] - 존재하지않는 tag 값입니다.", tag, e);
		}
		
		return bits;
	}

	public long getTf() {
		return tf;
	}

	public void setTf(long tf) {
		this.tf = tf;
	}

	public List<Keyword> getSubWords() {
		return subWords;
	}

	public void setSubWords(List<Keyword> subWords) {
		this.subWords = subWords;
	}
	
	public long getTagBit() {
		return tagBit;
	}

	public void setTagBit(long tagBit) {
		this.tagBit = tagBit;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		String subWord = "";
		if(subWords != null){
			subWord = ", " + subWords;
		}
		
		return "(seq : " + seq + ", word : " + word + ", tag : " + tag +  ", tf : " + tf + subWord 
//				+ ", tf=" + tf + ", desc=" + desc + ", subWords=" + subWords
//				+ ", tagBits=" + StringUtils.leftPad(Long.toBinaryString(tagBits), 64,"0")
				+ ")";
	}
	

}
