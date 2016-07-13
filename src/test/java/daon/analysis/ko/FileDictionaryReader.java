package daon.analysis.ko;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.test.BaseDictionary;

public class FileDictionaryReader<T> implements DictionaryReader<T> {
	
	private Logger logger = LoggerFactory.getLogger(FileDictionaryReader.class);

	private String encoding = Charset.defaultCharset().name();

	private List<String> lines;

	private int size = 0;

	private int cursor = 0; // index of next element to return

	private static ObjectMapper mapper = new ObjectMapper();
	
	private InputStream inputStream; 
	

	/**
	 * 파일 라인을 읽어 list로 준비함.
	 * - 사전 단어 기준 오름차순(asc) 정렬 
	 */
	public void read(Config config) throws IOException {

		String fileName = config.get(Config.FILE_NAME, String.class);
		
		inputStream = this.getClass().getResourceAsStream(fileName);
		if (inputStream == null)
			throw new FileNotFoundException("Not in classpath: " + fileName);

		try {
			lines = IOUtils.readLines(inputStream, Charsets.toCharset(encoding));

			//임시 중복제거
			Set<String> set = new HashSet<String>(lines);
			size = set.size();
			
			lines = new ArrayList<String>(set);
 			
			size = lines.size();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

		logger.info("read complete");
		
		Collections.sort(lines, new Comparator<String>() {
			@Override
			public int compare(String left, String right) {
				return left.compareTo(right);
			}
		});
		
		logger.info("sort complete");
	}

	public boolean hasNext() {
		return cursor != size;
	}

	public T next(Class<T> clazz) throws IOException {

		if(cursor >= size){
			return null;
		}
		
		String line = lines.get(cursor);

//		System.out.println(line);
		
		if(StringUtils.isBlank(line)){
			return null;
		}
		
		cursor++;
		
		T term = mapper.readValue(line, clazz);

		return term;
	}

	public int getCusor() {
		return cursor;
	}

	@Override
	public void close() {
		if(inputStream != null){
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
