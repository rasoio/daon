package daon.analysis.ko.dict;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.connect.ConnectionCosts;
import daon.analysis.ko.dict.fst.KeywordFST;
import daon.analysis.ko.dict.reader.Reader;
import daon.analysis.ko.dict.rule.Merger;
import daon.analysis.ko.dict.rule.Merger.Summary;
import daon.analysis.ko.dict.rule.MergerBuilder;
import daon.analysis.ko.dict.rule.operator.Operator;
import daon.analysis.ko.dict.rule.operator.PredicativeParticleEndingOperator;
import daon.analysis.ko.dict.rule.operator.PrefinalEndingOperator;
import daon.analysis.ko.dict.rule.operator.VerbEndingOperator;
import daon.analysis.ko.dict.rule.validator.PredicativeParticleEndingVaildator;
import daon.analysis.ko.dict.rule.validator.Validator;
import daon.analysis.ko.dict.rule.validator.VerbEndingVaildator;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.util.Utils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.IntSequenceOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DictionaryBuilder {

	private Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);

	private Config config = new Config();
	private Reader<Keyword> reader;
	private ConnectionCosts connectionCosts;

	public static DictionaryBuilder create() {
		return new DictionaryBuilder();
	}

	private DictionaryBuilder() {}

	public final DictionaryBuilder setFileName(final String fileName) {
		this.config.define(Config.FILE_NAME, fileName);
		return this;
	}

	public final DictionaryBuilder setReader(final Reader<Keyword> reader) {
		this.reader = reader;
		return this;
	}
	
	public final DictionaryBuilder setValueType(final Class<Keyword> valueType) {
		this.config.define(Config.VALUE_TYPE, valueType);
		return this;
	}

	public final DictionaryBuilder setConnectionCosts(final ConnectionCosts connectionCosts) {
		this.connectionCosts = connectionCosts;
		return this;
	}

	public Dictionary build() throws IOException{
		
		if(reader == null){
			//TODO throw exception 
		}
		
		try{
			reader.read(config);
			
//			logger.info("reader read complete");
			
			/**
			 * 사전 전처리
			 */
			StopWatch watch = new StopWatch();
			StopWatch totalWatch = new StopWatch();
			
			watch.start();
			totalWatch.start();
			
			//사전 목록 
			List<KeywordRef> keywordRefs = new ArrayList<KeywordRef>();
			
			//validator set
			Validator verbEnding = new VerbEndingVaildator();
			Validator paticleEnding = new PredicativeParticleEndingVaildator();
			
			//operator set
			Operator paticleEndingOp = new PredicativeParticleEndingOperator();
			Operator verbEndingOp = new VerbEndingOperator();
			Operator prefinalEndingOp = new PrefinalEndingOperator();
			
			List<Merger> mergeRules = new ArrayList<Merger>();
			
			//조합이 너무 많음...
//			Merger npRule = MergerBuilder.create().setDesc("n+p").build();
			
			boolean isMerging = false;
			boolean isDebug = false;

			Merger veRule = MergerBuilder.create().setDesc("v+e").setValidator(verbEnding).setOperator(verbEndingOp).setDebug(isDebug).build();
			Merger peRule = MergerBuilder.create().setDesc("p+e").setValidator(paticleEnding).setOperator(paticleEndingOp).setDebug(isDebug).build();
			Merger epeRule = MergerBuilder.create().setDesc("ep+e").setOperator(prefinalEndingOp).setDebug(isDebug).build();
			
			mergeRules.add(veRule);
			mergeRules.add(peRule);
			mergeRules.add(epeRule);
			
			//전체 사전 정보
			while (reader.hasNext()) {
				Keyword keyword = reader.next();
				
//				if(Utils.isTag(keyword, POSTag.n)){
//					//종성이 없는 체언
//					if(Utils.endWithNoJongseong(keyword)){
//						npRule.addPrevList(keyword);
//					}
//				}
				
				if(Utils.isTag(keyword, POSTag.p)){

					//복합키워드는 조합 시 제외
//					if(keyword.getTag().length() == 2){
//					
//						//조사가 종성으로 시작하는 경우( 조사인 경우 'ㄴ', 'ㄹ' 밖에 없음.. 다행 )
//						if(keyword.getWord().startsWith("ㄴ")){
//							npRule.addNextList(keyword);
//						}
//						
//						if(keyword.getWord().startsWith("ㄹ")){
//							npRule.addNextList(keyword);
//						}
//					}
					
					if(Utils.isTag(keyword, POSTag.pp)){
						peRule.addPrevList(keyword);
					}
				}
				
				if(Utils.isTag(keyword, POSTag.v) || Utils.isTag(keyword, POSTag.xj) || Utils.isTag(keyword, POSTag.xv)){
					
					veRule.addPrevList(keyword);
				}
				
				if(Utils.isTag(keyword, POSTag.e)){

					if(Utils.isTag(keyword, POSTag.ep)){
						epeRule.addPrevList(keyword);
					}else{
						epeRule.addNextList(keyword);
					}

					veRule.addNextList(keyword);

					peRule.addNextList(keyword);

				}

				//기본 사전 정보
				KeywordRef ref = new KeywordRef(keyword);
				keywordRefs.add(ref);
			}
			
			watch.stop();
			
			logger.info("dictionary added : {} ms, size :{}", watch.getTime(), keywordRefs.size());
			
			watch.reset();
			watch.start();

			if(isMerging){
                //조합 키워드 추가
                for(Merger rule : mergeRules){
                    addMergeSet(rule, keywordRefs);
                }
			}
			
			watch.stop();
			
			logger.info("fst pre load : {} ms, size :{}", watch.getTime(), keywordRefs.size());
			
			watch.reset();
			watch.start();
			
			Collections.sort(keywordRefs);

			watch.stop();
			
			logger.info("keywordRefs sorted : {} ms, size :{}", watch.getTime(), keywordRefs.size());

			watch.reset();
			watch.start();
			
			//seq 별 Keyword
//			PositiveIntOutputs fstOutput = PositiveIntOutputs.getSingleton();
			IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();
			Builder<IntsRef> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE4, fstOutput);
//			Builder<IntsRef> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE4, 0, 0, true, true, Integer.MAX_VALUE, fstOutput, true, PackedInts.COMPACT, true, 15);
//			Builder<Long> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);
			
			Map<IntsRef,IntsRef> fstData = new LinkedHashMap<IntsRef,IntsRef>();
//			Map<IntsRef,IntsRef> fstData = new TreeMap<IntsRef,IntsRef>();
			
			//중복 제거, 정렬, output append
			for(int idx=0,len = keywordRefs.size(); idx < len; idx++){
				
				IntsRefBuilder curOutput = new IntsRefBuilder();
				
				KeywordRef keyword = keywordRefs.get(idx);
				
				if(keyword == null){
					continue;
				}
				
//				logger.info("input : {}", keyword.getInput());
				
				final IntsRef input = keyword.getInput();
				
				IntsRef output = fstData.get(input);
				
				if(output != null){
					curOutput.copyInts(output);
				}
				
				curOutput.append(idx);
				output = curOutput.get();

				//fst 추가, output 사용이 애매..
				fstData.put(input, output);

				keyword.clearInput();
			}

			watch.stop();
			
			logger.info("fstData load : {} ms, size :{}", watch.getTime(), fstData.size());
			
			watch.reset();
			watch.start();

			for(Map.Entry<IntsRef,IntsRef> e : fstData.entrySet()){
				fstBuilder.add(e.getKey(), e.getValue());

//				logger.info("input : {} , output :{}", e.getKey(), e.getValue());
			}
			
//			fstData.clear();
			
			KeywordFST fst = new KeywordFST(fstBuilder.finish());
			
			watch.stop();
			
			logger.info("fst build : {} ms", watch.getTime());

			totalWatch.stop();
			
			logger.info("total : {} ms", totalWatch.getTime());

			Dictionary dictionary = new BaseDictionary(fst, keywordRefs, connectionCosts);

			return dictionary;

		} finally {
			reader.close();
		}
	}

	private void addMergeSet(Merger merger, List<KeywordRef> keywordRefs) {

		merger.merge(keywordRefs);
		
		if(merger.isDebug()){
			List<Summary> summaries = merger.getSummaries();
			
			logger.info("############# {} ############", merger.getDesc());
			
			summaries.stream().forEach(s -> {
				logger.info("{}", s); 
			});
			
			IntSummaryStatistics prevStats = summaries.stream().collect(Collectors.summarizingInt(Summary::getPrevCntInt));
			IntSummaryStatistics nextStats = summaries.stream().collect(Collectors.summarizingInt(Summary::getNextCntInt));
			
			IntSummaryStatistics loopStats = summaries.stream().collect(Collectors.summarizingInt(Summary::getLoopCntInt));
			IntSummaryStatistics refStats = summaries.stream().collect(Collectors.summarizingInt(Summary::getRefCntInt));
			
			logger.info("prev : {}",prevStats);
			logger.info("next : {}",nextStats);
			logger.info("loop : {}",loopStats);
			logger.info("refs : {}",refStats);
		}

	}
}
