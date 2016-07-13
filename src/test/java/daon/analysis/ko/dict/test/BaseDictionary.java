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
import daon.analysis.ko.Word;

/**
 * Base class for a binary-encoded in-memory dictionary.
 */
public class BaseDictionary implements Dictionary {
	
	private Logger logger = LoggerFactory.getLogger(BaseDictionary.class);

	private TokenInfoFST fst;

	private Word[] data;
//	private List<Word> data = new ArrayList<Word>();

	protected BaseDictionary(TokenInfoFST fst, Word[] data) throws IOException {
		this.fst = fst; 
		this.data = data; 
	}

	@Override
	public Word getWord(int wordId) {
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
					
					Word word = getWord(finalOutput);
					
					Term term = new Term(word, startOffset, word.getWord().length());
					
					results.add(term);
					
				} else {
					// System.out.println("?");
				}
			}
		}

		return results;
	}
}
