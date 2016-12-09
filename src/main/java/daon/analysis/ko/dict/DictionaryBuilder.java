package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.DicType;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.reader.DictionaryReader;
import daon.analysis.ko.dict.rule.MergeRule;
import daon.analysis.ko.dict.rule.MergeRuleBuilder;
import daon.analysis.ko.dict.rule.operator.Operator;
import daon.analysis.ko.dict.rule.operator.PredicativeParticleEndingOperator;
import daon.analysis.ko.dict.rule.operator.PrefinalEndingOperator;
import daon.analysis.ko.dict.rule.operator.VerbEndingOperator;
import daon.analysis.ko.dict.rule.validator.PredicativeParticleEndingVaildator;
import daon.analysis.ko.dict.rule.validator.Vaildator;
import daon.analysis.ko.dict.rule.validator.VerbEndingVaildator;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.MergeInfo;
import daon.analysis.ko.util.Utils;

public class DictionaryBuilder {

	private Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);

	private Config config = new Config();
	private DictionaryReader reader;
	
	public static DictionaryBuilder create() {
		return new DictionaryBuilder();
	}

	private DictionaryBuilder() {}

	public final DictionaryBuilder setFileName(final String fileName) {
		this.config.define(Config.FILE_NAME, fileName);
		return this;
	}

	public final DictionaryBuilder setDicType(final DicType type) {
		this.config.define(Config.DICTIONARY_TYPE, type);
		return this;
	}
	
	public final DictionaryBuilder setReader(final DictionaryReader reader) {
		this.reader = reader;
		return this;
	}
	
	public Dictionary build() throws IOException{
		
		if(reader == null){
			//TODO throw exception 
		}
		
		try{
			reader.read(config);
			
			logger.info("reader read complete");
			
			/**
			 * 사전 전처리
			 */
			StopWatch watch = new StopWatch();
			
			watch.start();
			
			//fst 키워드, 후보 데이터셋 구조(키워드 당 후보셋은 n개 후보셋 구조 정의 => 후보셋 한개 당 Keyword 1 or n 구조) 
			//후보셋은 long 값으로 참조 가능해야됨... Keyword 도 참조 가능한 구조 필요...
			//총 후보셋, Keyword 셋 두개 필요... 후보셋은 메모리를 최대한 적게 참조하는 구조로 
			
			//각종 셋들.. 
			List<KeywordRef> keywordRefs = new ArrayList<KeywordRef>();
//			List<Keyword> all = new ArrayList<Keyword>();
			
			//체언
//			제4장 제1절 체언과 조사 - 제14항 체언은 조사와 구별하여 적는다.
			
			//용언
//			제4장 제2절 어간과 어미 - 제18항 다음과 같은 용언들은 어미가 바뀔 경우, 그 어간이나 어미가 원칙에 벗어나면 벗어나는 대로 적는다.
//			https://www.korean.go.kr/front/page/pageView.do?page_id=P000069&mn_id=30
			
//			1. 어간의 끝 ‘ㄹ’이 줄어질 적
//			조합을 위한 데이터 구조..
			// Map key : 조합규칙, value : 앞단어 목록, 뒷단어 목록
			
			//validator set
			Vaildator verbEnding = new VerbEndingVaildator();
			Vaildator paticleEnding = new PredicativeParticleEndingVaildator();
			
			//operator set
			Operator paticleEndingOp = new PredicativeParticleEndingOperator();
			Operator verbEndingOp = new VerbEndingOperator();
			Operator prefinalEndingOp = new PrefinalEndingOperator();
			
			List<MergeRule> mergeRules = new ArrayList<MergeRule>();
			
			MergeRule npRule = MergeRuleBuilder.create().setDesc("n+p").build();
			
			MergeRule veRule = MergeRuleBuilder.create().setDesc("v+e").add(verbEnding).add(verbEndingOp).build();
			MergeRule peRule = MergeRuleBuilder.create().setDesc("p+e").add(paticleEnding).add(paticleEndingOp).build();
			MergeRule epeRule = MergeRuleBuilder.create().setDesc("ep+e").add(prefinalEndingOp).build();
			
			mergeRules.add(veRule);
			mergeRules.add(peRule);
			mergeRules.add(epeRule);
			
			//전체 사전 정보
			Map<Long,Keyword> dic = new HashMap<Long,Keyword>();
			
			while (reader.hasNext()) {
				Keyword keyword = reader.next();
				
				if(Utils.isTag(keyword, POSTag.n)){
					//종성이 없는 체언
					if(Utils.endWithNoJongseong(keyword)){
						npRule.addPrevList(keyword);
					}
				}
				
				if(Utils.isTag(keyword, POSTag.p)){

					//복합키워드는 조합 시 제외
					if(keyword.getTag().length() == 2){
					
						//조사가 종성으로 시작하는 경우( 조사인 경우 'ㄴ', 'ㄹ' 밖에 없음.. 다행 )
						if(keyword.getWord().startsWith("ㄴ")){
							npRule.addNextList(keyword);
						}
						
						if(keyword.getWord().startsWith("ㄹ")){
							npRule.addNextList(keyword);
						}
					}
					
					if(Utils.isTag(keyword, POSTag.pp)){
						peRule.addPrevList(keyword);
					}
				}
				
				if(Utils.isTag(keyword, POSTag.v)){
					
					veRule.addPrevList(keyword);
					

//					char[] end = Utils.getCharAtDecompose(keyword, -1);
//					
//					if(Utils.isMatch(end, jongseong, 2)){
//						euvcnt++;
//					}
				}
				
				if(Utils.isTag(keyword, POSTag.e)){
					
					//tag 조합은 제외 
					if(keyword.getTag().length() == 2){
						
						if(Utils.isTag(keyword, POSTag.ep)){
							epeRule.addPrevList(keyword);
						}else{
							epeRule.addNextList(keyword);
						}
						
						veRule.addNextList(keyword);
	
						peRule.addNextList(keyword);

					}
					
//					char[] start = Utils.getCharAtDecompose(keyword, 0);
//					if(eur.run(start, 0, start.length)){
//						euecnt++;
//					}
				}
				
				KeywordRef ref = new KeywordRef(keyword, keyword.getSeq());
				keywordRefs.add(ref);
				
				dic.put(keyword.getSeq(), keyword);
			}
			
//			System.out.println("eu v : " + veRule.getPrevList().size() + " => " + euvcnt);
//			System.out.println("eu e : " + veRule.getNextList().size() + " => " + euecnt);
			
			//조합 키워드 추가
			for(MergeRule rule : mergeRules){
//				addMergeSet(rule, keywordRefs);
			}
			
//			logger.info("cnt : {}", dropLSet.getPrevList().size());
//			for(Keyword k : dropLSet.getPrevList()){
//				logger.info("keyword : {}", k);
//			}
			
//			eTest.stream().collect(Collectors.groupingBy(
//                    Function.identity(), Collectors.counting()
//            )).entrySet().stream().forEach(s -> System.out.println("ec forEach: " + s));
//			
//			all.stream().collect(Collectors.groupingBy(
//                    Keyword::getDesc, Collectors.counting()
//            )).entrySet().stream().forEach(k -> System.out.println(k.getKey() + "	" + k.getValue()));
						
//			logger.info("gen cnt : {}", cnt);
			
			//대표어, 후보 목록 셋 구성
			/**
			 * 체언
			 * 종성이 없는 케이스 + 조사가 종성으로 시작하는 케이스
			 */

			
			/**
			 * 용언
			 * 1. 종성이 없는 케이스 + 어미가 종성으로 시작하는 케이스
			 * 2. 불규칙 용언 처리
			 * 
			 */

			//대표어 기준 집계 및 정렬
			/**
			 * 대표어 기준 후보셋 집계
			 * 
			 * 대표어 기준 오름차순 정렬
			 */
			
			watch.stop();
			
			logger.info("fst pre load : {} ms, size :{}", watch.getTime(), keywordRefs.size());
			watch.reset();
			watch.start();
			
			//seq 별 Keyword
			PositiveIntOutputs fstOutput = PositiveIntOutputs.getSingleton();
//			IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();
//			Builder<IntsRef> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE4, fstOutput);
			Builder<Long> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);
			
			Map<IntsRef,IntsRef> fstData = new TreeMap<IntsRef,IntsRef>();
			
			for(int idx=0,len = keywordRefs.size(); idx < len; idx++){
				IntsRefBuilder curOutput = new IntsRefBuilder();
				
				KeywordRef keyword = keywordRefs.get(idx);
				
				if(keyword == null){
					continue;
				}
				
				final IntsRef input = keyword.getInput();
				
				IntsRef output = fstData.get(input);
				
				if(output != null){
					curOutput.copyInts(output);
				}
				
				curOutput.append(idx);
				output = curOutput.get();

				//fst 추가, output 사용이 애매..
//				logger.info("word : {}, input : {}, output : {}", word, input, output);
				
//				if(idx % 10000 == 0){
//					System.out.println(idx);
//				}
				
				fstData.put(input, output);
			}
			
//			IntsRef[] outputs = fstData.values().toArray(new IntsRef[fstData.size()]);
			IntsRef[] outputs = new IntsRef[fstData.size()];
			
			logger.info("outputs length : {}", outputs.length);
//			values.
			logger.info("fstData complete");
			
			logger.info("fstData load : {} ms, size :{}", watch.getTime(), fstData.size());
			
			watch.reset();
			watch.start();
			

			long idx = 0;
			for(Map.Entry<IntsRef,IntsRef> e : fstData.entrySet()){
				outputs[(int)idx] = e.getValue();
				fstBuilder.add(e.getKey(), idx);
				idx++;

//				logger.info("input : {} , output :{}", e.getKey(), e.getValue());
			}
			
			KeywordFST fst = new KeywordFST(fstBuilder.finish());
			
			watch.stop();
			
			logger.info("fst load : {} ms", watch.getTime());
			
			return new BaseDictionary(fst, dic, keywordRefs, outputs);

		} finally {
			reader.close();
		}
	}

	private void addMergeSet(MergeRule rule, List<KeywordRef> keywordRefs) {
		int loopCnt = 0;
		int validationFailCnt = 0;
		int generateCnt = 0;
		
		//mergeSet 조합.
		/*
		rule.getPrevList().parallelStream().forEach((prev) -> {
			rule.getNextList().parallelStream().forEach((next) -> {
//				loopCnt.;
				
				List<Vaildator> validators = rule.getValidators();
				List<Operator> operators = rule.getOperators();
				
				boolean isValidated = true;
				for(Vaildator validator : validators){
					if(!validator.validate(prev, next)){
//						validationFailCnt++;
						isValidated = false;
						break;
					}
				}
				
				if(isValidated){
					for(Operator operator : operators){
						List<Keyword> results = operator.merge(prev, next);
						
						
						for(Keyword keyword : results){
//							generateCnt++;

							if(results.size() > 1){
								logger.info("keyword : {}, prev : {}, next : {}, results : {}", keyword.getWord(), prev, next);
//								results.stream().
							}
							all.add(keyword);
						}
					}
				}
			});
		});
		*/
		
		for(Keyword prev : rule.getPrevList()){
			
			for(Keyword next : rule.getNextList()){
				loopCnt++;
				
				//체크용 객체 생성..
				MergeInfo info = new MergeInfo(prev, next);
				
				List<Vaildator> validators = rule.getValidators();
				List<Operator> operators = rule.getOperators();
				
				boolean isValidated = true;
				for(Vaildator validator : validators){
					if(!validator.validate(info)){
						validationFailCnt++;
						isValidated = false;
						break;
					}
				}
				
				if(isValidated){
					for(Operator operator : operators){
						List<KeywordRef> results = operator.merge(info);
						
//						boolean is = true;
//						for(Keyword keyword : results){
//							if("IrrConjYEO".equals(keyword.getDesc())){
//								is = true;
//							}
//						}
						
						for(KeywordRef keyword : results){
							generateCnt++;

//							if(is){
//								if(results.size() > 1){
//									logger.info("keyword : {}, desc : {}, prev : {}, next : {}, results : {}", keyword.getWord(), keyword.getDesc(), prev, next);
//								}
//							}
							keywordRefs.add(keyword);
						}
					}
				}
			}
		}
		
		logger.info("rule : {}, generateCnt : {}, prev cnt : {}, next cnt : {}, loopCnt : {}, validationFailCnt : {}", rule.getDesc(), generateCnt, rule.getPrevList().size(), rule.getNextList().size(), loopCnt, validationFailCnt);
	}
}
