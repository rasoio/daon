package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.FST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.Term;

/**
 * Base class for a binary-encoded in-memory dictionary.
 */
public class BaseDictionary implements Dictionary {
	
	private Logger logger = LoggerFactory.getLogger(BaseDictionary.class);

	private KeywordFST fst;

	//원본 참조용 (seq, keyword)
	private Map<Long,Keyword> dictionary;
	private List<KeywordRef> keywordRefs;
	private IntsRef[] outputs;

	protected BaseDictionary(KeywordFST fst, Map<Long,Keyword> dictionary, List<KeywordRef> keywordRefs, IntsRef[] outputs) throws IOException {
		this.fst = fst; 
		this.dictionary = dictionary; 
		this.keywordRefs = keywordRefs; 
		this.outputs = outputs; 
	}
	
	@Override
	public KeywordRef getKeywordRef(int idx){
		return keywordRefs.get(idx);
	}
	
	@Override
	public Keyword getKeyword(long seq) {
		return dictionary.get(seq);
	}
	
	public Map<Integer, List<Term>> lookup(char[] chars, int off, int len) throws IOException {

		//offset 별 기분석 사전 Term 추출 결과
		Map<Integer, List<Term>> results = new HashMap<Integer, List<Term>>();

		final FST.BytesReader fstReader = fst.getBytesReader();

		FST.Arc<Long> arc = new FST.Arc<>();
		
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
				
//				logger.info("match output={}", output);
				
				if(logger.isDebugEnabled()){
					logger.debug("match output={}", output);
				}
				
				if (arc.isFinal()) {
					
//					final IntsRef wordIds = fst.getInternalFST().outputs.add(output, arc.nextFinalOutput);

//					final String word = new String(chars, startOffset, (i + 1));
					
					final Long idx = output + arc.nextFinalOutput.longValue();
//					final String wordId = output + arc.nextFinalOutput.toString();
					
//					final Long wordSet = arc.nextFinalOutput.wordSet;
//					final List<Long> wordSets = arc.nextFinalOutput.wordSets;

//					logger.info("str : {}, char : {}, wordId : {}, arc : {}", word, (char) ch, idx, arc);
//					logger.info("str : {}, char : {}, startOffset : {}, currentOffset : {}", new String(chars, startOffset, (i + 1)), (char) ch, startOffset, (startOffset + (i + 1)));
					
					if(logger.isDebugEnabled()){
//						logger.debug("wordId : {}, arc : {}", wordIds, arc);
						
//						new String(chars, startOffset, (startOffset + (i + 1)))
						logger.debug("str : {}, char : {}, startOffset : {}, currentOffset : {}", new String(chars, startOffset, (i + 1)), (char) ch, startOffset, (startOffset + (i + 1)));
					}
					
					
//					addResults(results, startOffset, idx.intValue());
					
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
	private void addResults(Map<Integer, List<Term>> results, int startOffset, final int outputIdx) {
		IntsRef output = outputs[outputIdx];
		
//		logger.info("outputIdx : {}, output : {}", outputIdx, output.ints);
		
		//wordId(seq)에 해당하는 Keyword 가져오기
		for(int i = 0; i < output.length; i++){
			int idx = output.ints[i];
			
			KeywordRef ref = getKeywordRef(idx);
			for(long wordId : ref.getWordIds()){
				Keyword w = getKeyword(wordId);
				
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
		}
	}
	
	public List<KeywordRef> getData(){
		return keywordRefs;
	}
}
