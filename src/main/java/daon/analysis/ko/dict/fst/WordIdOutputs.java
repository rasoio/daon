package daon.analysis.ko.dict.fst;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.RamUsageEstimator;
import org.apache.lucene.util.fst.Outputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordIdOutputs extends Outputs<WordIdOutputs.Output> {

	private Logger logger = LoggerFactory.getLogger(WordIdOutputs.class);
	
	private final static Output NO_OUTPUT = new Output();

	private final static WordIdOutputs singleton = new WordIdOutputs();

	private WordIdOutputs() {}

	public static WordIdOutputs getSingleton() {
		return singleton;
	}
	
	public static final class Output {
		
		public final Long wordSet;
		
		public List<Long> wordSets = new ArrayList<Long>(0);
		
		public Output() {
			wordSet = null;
		}

		public Output(Long wordSet) {
			this.wordSet = wordSet;
			
			if(wordSet > 0){
				wordSets.add(wordSet);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((wordSet == null) ? 0 : wordSet.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Output other = (Output) obj;
			if (wordSet == null) {
				if (other.wordSet != null)
					return false;
			} else if (!wordSet.equals(other.wordSet))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Output [wordSet=" + wordSet + "]";
		}
		
		
	}

	@Override
	public Output common(Output output1, Output output2) {
//		logger.info("common output1 : {}, output2 : {}", output1, output2);
		
		assert valid(output1);
	    assert valid(output2);
	    
		if (output1 == NO_OUTPUT || output2 == NO_OUTPUT) {
			// if (TEST) System.out.println("ret:"+NO_OUTPUT);
			return NO_OUTPUT;
		} else {
	      assert output1.wordSet > 0;
	      assert output2.wordSet > 0;
	      return new Output(Math.min(output1.wordSet, output2.wordSet));
	    }
		
		
//		List<Long[]> wordSet = new ArrayList<Long[]>();
		
		
//		
//		if(first != NO_OUTPUT){
//			wordSet.addAll(first.wordSet);
//		}
//		
//		if(second != NO_OUTPUT){
//			wordSet.addAll(second.wordSet);
//		}
		
//		return new Output(wordSet);
	}

	@Override
	public Output subtract(Output output, Output inc) {

//		logger.info("subtract output : {}, inc : {}", output, inc);
		
		if (inc == NO_OUTPUT) {
	      //if (TEST) System.out.println("ret:"+t1);
	      return output;
	    }
		
		assert valid(output);
	    assert valid(inc);
	    assert output.wordSet >= inc.wordSet;

	    if (inc == NO_OUTPUT) {
			return output;
		} else if (output.equals(inc)) {
			return NO_OUTPUT;
		} else {
			return new Output(output.wordSet - inc.wordSet);
		}
	}

	@Override
	public Output add(Output prefix, Output output) {

//		logger.info("add prefix : {}, output : {}", prefix, output);
		
		assert valid(prefix);
	    assert valid(output);
	    if (prefix == NO_OUTPUT) {
	      return output;
	    } else if (output == NO_OUTPUT) {
	      return prefix;
	    } else {
	      return new Output(prefix.wordSet + output.wordSet);
	    }
	}

	@Override
	public void write(Output output, DataOutput out) throws IOException {
		// TODO Auto-generated method stub

//		logger.info("write output : {}, out : {}", output, out);
		
		assert valid(output);
	    out.writeVLong(output.wordSet);
	    /*
		List<Long[]> set = output.wordSet;
		int size = set.size();
		out.writeInt(size);
		
		for(int i=0; i<size; i++){
			Long[] wordIds = set.get(i);
			
			for(Long wordId : wordIds){
				out.writeVLong(i);
			}
		}
		*/
	}

	@Override
	public Output read(DataInput in) throws IOException {
//		logger.info("read in : {}", in);
		long v = in.readVLong();
	    if (v == 0) {
	      return NO_OUTPUT;
	    } else {
	      return new Output(v);
	    }
	}

	public Output merge(Output first, Output second) {

//		logger.info("merge first : {}, second : {}", first, second);
		
//		List<Long[]> wordSet = new ArrayList<Long[]>();
		
		Long i = 0l;
		
		Output output = new Output(i);
		
		if(first != NO_OUTPUT){
//			wordSet.addAll(first.wordSet);
			i += first.wordSet;
			
			output.wordSets.addAll(first.wordSets);
		}
		
		if(second != NO_OUTPUT){
//			wordSet.addAll(second.wordSet);
			i += second.wordSet;
			

			output.wordSets.addAll(second.wordSets);
		}
		
		return output;
	}

	private boolean valid(Output o) {
		assert o != null;
		assert o == NO_OUTPUT || o.wordSet > 0 : "o=" + o;
		return true;
	}

	@Override
	public String outputToString(Output output) {
		return output.toString();
	}

	@Override
	public long ramBytesUsed(Output output) {
		return RamUsageEstimator.shallowSizeOf(output);
	}

	@Override
	public Output getNoOutput() {
		return NO_OUTPUT;
	}
	
}
