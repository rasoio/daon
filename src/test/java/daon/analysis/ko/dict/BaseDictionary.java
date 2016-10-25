package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.fst.FST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.Keyword;
import daon.analysis.ko.Term;

/**
 * Base class for a binary-encoded in-memory dictionary.
 */
public class BaseDictionary implements Dictionary {
	
	private Logger logger = LoggerFactory.getLogger(BaseDictionary.class);

	private TokenInfoFST fst;

	private Map<Long,Keyword> data;

	protected BaseDictionary(TokenInfoFST fst, Map<Long,Keyword> data) throws IOException {
		this.fst = fst; 
		this.data = data; 
	}

	@Override
	public Keyword getWord(long wordId) {
		return data.get(wordId);
	}
	
	public Map<Integer, List<Term>> lookup(char[] chars, int off, int len) throws IOException {

		//offset 별 기분석 사전 Term 추출 결과
		Map<Integer, List<Term>> results = new HashMap<Integer, List<Term>>();

		final FST.BytesReader fstReader = fst.getBytesReader();

		FST.Arc<Long> arc = new FST.Arc<>();
		int end = off + len;
		for (int startOffset = off; startOffset < end; startOffset++) {
			arc = fst.getFirstArc(arc);
			long output = 0;
			int remaining = end - startOffset;
			
			if(logger.isDebugEnabled()){
				logger.debug("output={}", output);
			}

			for (int i = 0; i < remaining; i++) {
				int ch = chars[startOffset + i];

				if(logger.isDebugEnabled()){
					logger.debug("ch={}", chars[startOffset + i]);
				}

				if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
					break; // continue to next position
				}
				output += arc.output.longValue();
				if (arc.isFinal()) {
					final long wordId = output + arc.nextFinalOutput.longValue();

					if(logger.isDebugEnabled()){
						logger.debug("char : {}, start : {}", chars[startOffset + i], (startOffset + i));
					}
					
					addResults(results, startOffset, wordId);
					
				} else {
					// System.out.println("?");
				}
			}
		}

		return results;
	}

	/**
	 * 결과에 키워드 term 추가
	 * @param results
	 * @param startOffset
	 * @param wordId
	 */
	private void addResults(Map<Integer, List<Term>> results, int startOffset, final long wordId) {
		//wordId(seq)에 해당하는 Keyword 가져오기
		Keyword word = getWord(wordId);
		
		int offset = startOffset;
		int length = word.getWord().length();
		
		Term term = new Term(word, offset, length);
			
		List<Term> terms = results.get(offset);
			
		if(terms == null){
			terms = new ArrayList<Term>();
		}
			
		terms.add(term);
			
		results.put(offset, terms);
	}
}
