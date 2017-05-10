package daon.analysis.ko.config;

import daon.analysis.ko.dict.DictionaryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Config {

    private Logger logger = LoggerFactory.getLogger(Config.class);

    /**
     * 설정 parameter key 값 : 파일명
     */
    public static final String FILE_NAME = "fileName";

    /**
     * 설정 parameter key 값 : jdbc 정보
     */
    public static final String JDBC_INFO = "jdbcInfo";

    public static final String VALUE_TYPE = "valueType";

    private final Map<String, Object> configValues = new HashMap<>();

    public static Config create() {
        return new Config();
    }

    private Config() {}

    public Config define(String name, Object value) {
        if (configValues.containsKey(name)) {
            logger.warn("Configuration " + name + " is defined twice.");
        }

        configValues.put(name, value);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) configValues.get(key);
    }

    /**
     * 확률 기본값
     */
    public static final float DEFAULT_PROBABILITY = 50;

    /**
     * 연결 실패 기본값
     */
    public static final float MISS_PENALTY_SCORE = 100;

}
