package daon.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import daon.core.data.Morpheme;
import daon.core.data.Word;
import daon.core.util.Utils;
import daon.manager.model.param.WordFormParams;
import daon.manager.model.param.WordParams;
import daon.spark.words.UploadUserWords;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.SparkSession;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class WordService extends CommonService{

	@Autowired
	private TransportClient client;

	private static String INDEX = "words";
	private static String TYPE = "word";

	@Autowired
	private ObjectMapper mapper;

	public Map<String, Object> get(WordParams params) {
	    String index = params.getIndex();

		GetResponse response = client.prepareGet(index, TYPE, params.getId()).get();

		return response.getSourceAsMap();
	}

	public String search(WordParams params) throws IOException {

		BoolQueryBuilder mainQueryBuilder = boolQuery();


		String condition = params.getCondition();
		String surface = params.getSurface();
		String word = params.getWord();
		String[] indices = params.getIndices();

		addCondition(mainQueryBuilder, "surface", condition, surface);
		addConditionNested(mainQueryBuilder, "morphemes", "morphemes.word", condition, word);


		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX);

		if(indices != null && indices.length > 0){
			searchRequestBuilder = client.prepareSearch(indices);
		}

		searchRequestBuilder = searchRequestBuilder
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



	public String delete(WordFormParams params){

		String index = params.getIndex();
		String id = params.getId();

        DeleteResponse response = client.prepareDelete(index, TYPE, id)
//                .setOperationThreaded(false)
                .get();

		return response.toString();

    }


    public String upsert(WordFormParams params) throws Exception {

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

    private String toJson(WordFormParams params) throws Exception {

	    List<Morpheme> morphemes = Utils.parseMorpheme(params.getMorphemes());

		Word word = new Word();
		word.setSurface(params.getSurface());
		word.setMorphemes(morphemes);
		word.setWeight(params.getWeight());

		String jsonString = mapper.writeValueAsString(word);
		return jsonString;
	}


	public List<String> upload(InputStream input, String prefix, boolean isAppend) throws Exception {

		List<String> errors;

		try(SparkSession sparkSession = UploadUserWords.getSparkSession()){

			errors = UploadUserWords.execute(sparkSession, input, prefix, isAppend);
		}

		return errors;
	}


}