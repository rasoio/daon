package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.FST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.Term;

/**
 * Base class for a binary-encoded in-memory dictionary.
 */
public class BaseDictionary implements Dictionary {
	
	private Logger logger = LoggerFactory.getLogger(BaseDictionary.class);

	private KeywordFST fst;

	//원본 참조용 (seq, keyword)
	private Map<Long,Keyword> dictionary;
	private Map<String,List<Long[]>> data;

	protected BaseDictionary(KeywordFST fst, Map<Long,Keyword> dictionary, Map<String,List<Long[]>> data) throws IOException {
		this.fst = fst; 
		this.dictionary = dictionary; 
		this.data = data; 
	}

	@Override
	public Keyword getWords(Long seq) {
		return dictionary.get(seq);
	}
	
	public Map<Integer, List<Term>> lookup(char[] chars, int off, int len) throws IOException {

		//offset 별 기분석 사전 Term 추출 결과
		Map<Integer, List<Term>> results = new HashMap<Integer, List<Term>>();

		final FST.BytesReader fstReader = fst.getBytesReader();

		FST.Arc<Long> arc = new FST.Arc<>();
//		FST.Arc<IntsRef> arc = new FST.Arc<>();
//		FST.Arc<Output> arc = new FST.Arc<>();
		
		// Accumulate output as we go
//	    final IntsRef NO_OUTPUT = fst.outputs.getNoOutput();
//	    IntsRef output = NO_OUTPUT;
//	    
//	    int l = offset + length;
//	    try {
//	      for (int i = offset, cp = 0; i < l; i += Character.charCount(cp)) {
//	        cp = Character.codePointAt(word, i, l);
//	        if (fst.findTargetArc(cp, arc, arc, bytesReader) == null) {
//	          return null;
//	        } else if (arc.output != NO_OUTPUT) {
//	          output = fst.outputs.add(output, arc.output);
//	        }
//	      }
//	      if (fst.findTargetArc(FST.END_LABEL, arc, arc, bytesReader) == null) {
//	        return null;
//	      } else if (arc.output != NO_OUTPUT) {
//	        return fst.outputs.add(output, arc.output);
//	      } else {
//	        return output;
//	      }
//	    } catch (IOException bogus) {
//	      throw new RuntimeException(bogus);
//	    }
		
		
		int end = off + len;
		for (int startOffset = off; startOffset < end; startOffset++) {
			arc = fst.getFirstArc(arc);
//			String output = new String("");
//			IntsRef output = fst.getInternalFST().outputs.getNoOutput();
			Long output = new Long(0);
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
				
//				output += arc.output.toString();
				output += arc.output.longValue();
//				output = fst.getInternalFST().outputs.add(output, arc.output);
				
//				output.(arc.output.ints);
				
				logger.info("match output={}", output);
				
				if(logger.isDebugEnabled()){
					logger.debug("match output={}", output);
				}
				
				if (arc.isFinal()) {
					
//					final IntsRef wordId = fst.getInternalFST().outputs.add(output, arc.output);
					final Long wordId = output + arc.nextFinalOutput.longValue();
//					final String wordId = output + arc.nextFinalOutput.toString();
					
//					final Long wordSet = arc.nextFinalOutput.wordSet;
//					final List<Long> wordSets = arc.nextFinalOutput.wordSets;

					logger.info("wordId : {}, arc : {}", wordId, arc);
					logger.info("str : {}, char : {}, startOffset : {}, currentOffset : {}", new String(chars, startOffset, (i + 1)), (char) ch, startOffset, (startOffset + (i + 1)));
					
					if(logger.isDebugEnabled()){
						logger.debug("wordId : {}, arc : {}", wordId, arc);
						
//						new String(chars, startOffset, (startOffset + (i + 1)))
						logger.debug("str : {}, char : {}, startOffset : {}, currentOffset : {}", new String(chars, startOffset, (i + 1)), (char) ch, startOffset, (startOffset + (i + 1)));
					}
					
					final String word = new String(chars, startOffset, (i + 1));
					
					addResults(results, startOffset, word);
					
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
	private void addResults(Map<Integer, List<Term>> results, int startOffset, final String word) {
		/*
		//wordId(seq)에 해당하는 Keyword 가져오기
		List<Keyword> words = getWords(word);
		
		for(Keyword w : words){
			int offset = startOffset;
			int length = w.getWord().length();
			
			Term term = new Term(w, offset, length);
				
			List<Term> terms = results.get(offset);
				
			if(terms == null){
				terms = new ArrayList<Term>();
			}
				
			terms.add(term);
				
			results.put(offset, terms);
		}
		*/
	}
	
	public Map<String,List<Long[]>> getData(){
		return data;
	}
}
