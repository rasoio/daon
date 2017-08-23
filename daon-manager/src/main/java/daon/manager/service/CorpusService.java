package daon.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.model.Keyword;
import daon.manager.model.data.Eojeol;
import daon.manager.model.data.Morpheme;
import daon.manager.model.data.Sentence;
import daon.manager.model.data.Term;
import daon.manager.model.param.CorpusFormParams;
import daon.manager.model.param.CorpusParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class CorpusService {

	@Autowired
	private TransportClient client;

	private static String INDEX = "sentences";
	private static String TYPE = "sentence";

	@Autowired
	private ObjectMapper mapper;

	public Map<String, Object> get(CorpusParams params) {
	    String index = params.getIndex();

		GetResponse response = client.prepareGet(index, TYPE, params.getId()).get();

		return response.getSourceAsMap();
	}

	public String search(CorpusParams params) throws IOException {

		BoolQueryBuilder mainQueryBuilder = boolQuery();

		List<Term> checkTerms = params.getCheckTerms();

		if(checkTerms != null){
			for(Term checkTerm : checkTerms) {

				BoolQueryBuilder subQuery = boolQuery();

				//어절 조건 1, 형태소 조건 N
				NestedQueryBuilder eojeolQuery = nestedQuery("eojeols", subQuery, ScoreMode.None);

				if(StringUtils.isNoneBlank(checkTerm.getSurface())){
					subQuery.must(matchQuery("eojeols.surface", checkTerm.getSurface()));
				}else{
					subQuery.must(matchAllQuery());
				}

				if(checkTerm.getKeywords() != null){
					for(Keyword keyword : checkTerm.getKeywords()){
						subQuery.must(nestedQuery("eojeols.morphemes",
								boolQuery()
									.must(matchQuery("eojeols.morphemes.word", keyword.getWord()))
									.must(matchQuery("eojeols.morphemes.tag", keyword.getTag().getName())
								), ScoreMode.None));
					}
				}

				mainQueryBuilder.must(eojeolQuery);
			}
		}

		String sentence = params.getSentence();

		//TODO performance issue
		if(StringUtils.isNoneBlank(sentence)){
			mainQueryBuilder.must(wildcardQuery("sentence", "*" + sentence + "*"));
        }

		String eojeol = params.getEojeol();

		//TODO performance issue
		if(StringUtils.isNoneBlank(eojeol)){
			mainQueryBuilder.must(nestedQuery("eojeols", wildcardQuery("eojeols.surface", "*" + eojeol + "*"), ScoreMode.None));
		}

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery( mainQueryBuilder)
				.setFrom(params.getFrom())
				.setSize(params.getSize());

		log.info("query : {}", searchRequestBuilder);

//		if(filter.isRecent()) {
//			searchRequestBuilder.addSort("udate", SortOrder.DESC);
//		}

//		if(filter.isDebug()) {
//			searchRequestBuilder.setExplain(true).setProfile(true); // Debug mode
//		}

		SearchResponse response = searchRequestBuilder
				.execute()
				.actionGet();


		log.info("response : {}", response);

		return response.toString();
	}



	public String delete(CorpusFormParams params){

		String index = params.getIndex();
		String id = params.getId();

        DeleteResponse response = client.prepareDelete(INDEX, TYPE, id)
//                .setOperationThreaded(false)
                .get();

		return response.toString();

    }


    public String upsert(CorpusFormParams params) throws Exception {

	    String index = params.getIndex();
	    String id = params.getId();

	    //index 값이 없으면 에러 raise
		if(StringUtils.isBlank(index)){
			throw new Exception("index 값을 설정해주세요");
		}

	    if(StringUtils.isBlank(id)){
			id = createId();
		}

	    String json = toJson(params);


	    log.info("id : {}\nindex : {}\njson : {}", id, index, json);

        IndexRequest indexRequest = new IndexRequest(index, TYPE, id)
                .source(json, XContentType.JSON);
        UpdateRequest updateRequest = new UpdateRequest(index, TYPE, id)
                .doc(json, XContentType.JSON)
                .upsert(indexRequest);

		UpdateResponse updateResponse = client.update(updateRequest).get();

		return updateResponse.toString();
    }

    private String createId(){
		return UUIDs.base64UUID();
	}

    private String toJson(CorpusFormParams params) throws Exception {

		Sentence sentence = new Sentence();
		sentence.setSentence(params.getSentence());
		sentence.setEojeols(parse(params.getEojeols()));

		String jsonString = mapper.writeValueAsString(sentence);
		return jsonString;
	}

	/**
	 * 어절 구분 : 줄바꿈 문자
	 * 어절-형태소 간 구분 : ' - '
	 * 형태소 간 구분 : 공백(스페이스) 문자
	 * 형태소 내 단어-태그 구분 : '/'
	 */
	public List<Eojeol> parse(String eojeols) throws Exception{

		List<Eojeol> results = new ArrayList<>();

		String[] lines = StringUtils.split(eojeols, '\n');

		if(lines.length == 0){
			throw new Exception("어절 구분 분석 결과가 없습니다. (어절 구분자 줄바꿈 문자)");
		}

		for(int i=0, len = lines.length; i< len; i++){
			String line = lines[i];

			String[] surfaceAndMorphs = line.split("\\s+[-]\\s+");

			if(surfaceAndMorphs.length == 0){
				throw new Exception("어절-형태소 간 구분 값(' - ')이 없습니다.");
			}

			if(surfaceAndMorphs.length > 2){
				throw new Exception("어절-형태소 간 구분 값(' - ')은 한개만 있어야 됩니다.");
			}

			String surface = surfaceAndMorphs[0];

			String morph = surfaceAndMorphs[1];

			Eojeol eojeol = new Eojeol();
			eojeol.setSeq(i);
			eojeol.setSurface(surface);

			List<Morpheme> morphemes = parseMorpheme(morph);

			eojeol.setMorphemes(morphemes);

			results.add(eojeol);
		}

		return results;
	}

	private List<Morpheme> parseMorpheme(String m) throws Exception {

		List<Morpheme> results = new ArrayList<>();

		String[] morphes = m.split("\\s+");

		if(morphes.length == 0){
            throw new Exception("형태소 간 구분자 ' '가 없습니다.");
        }

		for(int i=0, len = morphes.length; i < len; i++){
			String morph = morphes[i];

			//형태소 내 단어-태그 구분자 '/' 분리
			String[] wordInfo = morph.split("[/](?=[A-Z]{2,3}$)");

			if(wordInfo.length != 2){
				throw new Exception("형태소 내 단어-태그 구분자('/')가 잘못 되었습니다.");
			}

			String word = wordInfo[0];
			String tag = wordInfo[1];

			try {
				tag = POSTag.valueOf(tag).getName();
			}catch (Exception e){
				throw new Exception(tag + " <= 태그값이 잘못되었습니다.");
			}

			Morpheme morpheme = new Morpheme(i, word, tag);

			results.add(morpheme);
        }

		if(results.size() == 0){
			throw new Exception("형태소 구분 분석 결과가 없습니다. (형태소 구분자 공백 문자)");
		}

        return results;
	}


}