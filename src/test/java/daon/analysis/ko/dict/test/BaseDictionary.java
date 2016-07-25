package daon.analysis.ko.dict.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.DictionaryReader;
import daon.analysis.ko.FileDictionaryReader;
import daon.analysis.ko.Term;
import daon.analysis.ko.Keyword;

/**
 * Base class for a binary-encoded in-memory dictionary.
 */
public class BaseDictionary implements Dictionary {
	
	private Logger logger = LoggerFactory.getLogger(BaseDictionary.class);

	private TokenInfoFST fst;

	private Keyword[] data;
//	private List<Word> data = new ArrayList<Word>();

	protected BaseDictionary(TokenInfoFST fst, Keyword[] data) throws IOException {
		this.fst = fst; 
		this.data = data; 
	}

	@Override
	public Keyword getWord(int wordId) {
//		return data.get(wordId);
		return data[wordId];
	}
	
	public List<Term> lookup(char[] chars, int off, int len) throws IOException {
		// TODO: can we avoid this treemap/toIndexArray?
		
		List<Term> results = new ArrayList<Term>();

		final FST.BytesReader fstReader = fst.getBytesReader();

		FST.Arc<Long> arc = new FST.Arc<>();
		int end = off + len;
		for (int startOffset = off; startOffset < end; startOffset++) {
			arc = fst.getFirstArc(arc);
			int output = 0;
			int remaining = end - startOffset;
			
			logger.debug("output={}", output);

			for (int i = 0; i < remaining; i++) {
				int ch = chars[startOffset + i];

				logger.debug("ch={}", chars[startOffset + i]);

				if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
					break; // continue to next position
				}
				output += arc.output.intValue();
				if (arc.isFinal()) {
					final int finalOutput = output + arc.nextFinalOutput.intValue();

					logger.debug("char : {}, start : {}", chars[startOffset + i], (startOffset + i));
					
					Keyword word = getWord(finalOutput);
					
					Term term = new Term(word, startOffset, word.getWord().length());
					
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
