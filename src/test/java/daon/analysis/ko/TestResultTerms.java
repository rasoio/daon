package daon.analysis.ko;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import daon.analysis.ko.dict.BaseDictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.dict.reader.FileReader;
import daon.analysis.ko.model.Keyword;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestResultTerms {
	
	private Logger logger = LoggerFactory.getLogger(TestResultTerms.class);
	
	@Test
	public void testArray() {

		Position[] i = new Position[10];

		Position t = i[9];

		logger.info("t : {}", t);
	}

	class Position {

	}
	
}
