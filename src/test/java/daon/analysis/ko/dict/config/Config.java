package daon.analysis.ko.dict.config;

import java.util.HashMap;
import java.util.Map;

public class Config {

	
	public static final String DICTIONARY_TYPE = "dicType";
	
	public static final String FILE_NAME = "fileName";
	
	public static final String JDBC_INFO = "jdbcInfo";
	
	private final Map<String, Object> configValues = new HashMap<>();

	public void define(String name, Object value) {
		if (configValues.containsKey(name)) {
			//throw new ConfigException("Configuration " + name + " is defined twice.");
		}
		
		configValues.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> type){
		return (T) configValues.get(key);
	}

	/**
	 * The dictionary types
	 */
	public enum DicType {
		KKM("KKM"), // 꼬꼬마
		N("Noun"), // 체언
		V("Verb"), // 용언
		M("Adverb"), // 수식언, 독립언
		J("Josa"), // 관계언
		E("Eomi") // 용언 어미
		;

		private String name;

		DicType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
