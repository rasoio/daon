package daon.analysis.ko.dict.test;

import java.io.IOException;
import java.util.ArrayList;
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
	
	public List<Term> lookup(char[] chars, int off, int len) throws IOException {

		List<Term> results = new ArrayList<Term>();

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
					
					//wordId(seq)에 해당하는 Keyword 가져오기
					Keyword word = getWord(wordId);
					
					Term term = new Term(word, startOffset, word.getWord().length());
					
					//prev, next 연결.. 삭제 예정
					int size = results.size();
					if(size > 0){
						Term prevTerm = results.get(size - 1);
						
						term.setPrevTerm(prevTerm);
						
						prevTerm.setNextTerm(term);
					}
					
					results.add(term);
					
				} else {
					// System.out.println("?");
				}
			}
		}

		return results;
	}
}
