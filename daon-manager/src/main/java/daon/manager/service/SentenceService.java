package daon.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import daon.core.data.Eojeol;
import daon.core.data.Sentence;
import daon.core.result.Keyword;
import daon.core.util.Utils;
import daon.manager.model.param.TermParams;
import daon.manager.model.param.SentenceFormParams;
import daon.manager.model.param.SentenceParams;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class SentenceService extends CommonService{

	@Autowired
	private TransportClient client;

	private static String INDEX = "sentences";
	private static String TYPE = "sentence";

	@Autowired
	private ObjectMapper mapper;

	public Map<String, Object> get(SentenceParams params) {
	    String index = params.getIndex();

		GetResponse response = client.prepareGet(index, TYPE, params.getId()).get();

		return response.getSourceAsMap();
	}

	public String search(SentenceParams params) throws IOException {

		BoolQueryBuilder mainQueryBuilder = boolQuery();

		List<TermParams> checkTerms = params.getCheckTerms();

		if(checkTerms != null){
			for(TermParams checkTerm : checkTerms) {

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

        String condition = params.getCondition();
		String sentence = params.getSentence();
		String eojeol = params.getEojeol();
        String[] indices = params.getIndices();

		addCondition(mainQueryBuilder, "sentence", condition, sentence);
        addConditionNested(mainQueryBuilder, "eojeols", "eojeols.surface", condition, eojeol);


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



	public String delete(SentenceFormParams params){

		String index = params.getIndex();
		String id = params.getId();

        DeleteResponse response = client.prepareDelete(index, TYPE, id)
//                .setOperationThreaded(false)
                .get();

		return response.toString();

    }


    public String upsert(SentenceFormParams params) throws Exception {

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

    private String toJson(SentenceFormParams params) throws Exception {

	    List<Eojeol> eojeols = Utils.parse(params.getEojeols());

		Sentence sentence = new Sentence();
		sentence.setSentence(params.getSentence());
		sentence.setEojeols(eojeols);

		String jsonString = mapper.writeValueAsString(sentence);
		return jsonString;
	}


}