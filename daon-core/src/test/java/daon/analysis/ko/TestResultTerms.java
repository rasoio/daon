package daon.analysis.ko;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
