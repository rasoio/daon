package daon.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import daon.core.config.POSTag;
import daon.manager.model.param.SentenceFormParams;
import daon.manager.model.param.TagFormParams;
import daon.manager.model.param.TagParams;
import daon.manager.model.param.WordParams;
import daon.spark.model.MakeModel;
import daon.spark.tags.TagTrans;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class TagService {

	@Autowired
	private TransportClient client;

	private static String INDEX = "tag_trans";
	private static String TYPE = "tag";

	@Autowired
	private ObjectMapper mapper;

	public String search(TagParams params) throws IOException {

		BoolQueryBuilder mainQueryBuilder = boolQuery();

		List<String> positions = params.getPosition();

		if(positions != null && positions.size() > 0){
			mainQueryBuilder.must(termsQuery("position", positions));
        }

		String tag1 = params.getTag1();

		if(StringUtils.isNoneBlank(tag1)){
			mainQueryBuilder.must(matchQuery("tag1", tag1));
		}

		String tag2 = params.getTag2();

		if(StringUtils.isNoneBlank(tag2)){
			mainQueryBuilder.must(matchQuery("tag2", tag2));
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

	public String save(TagFormParams params) throws Exception {
		String index = INDEX;
		String id = params.getId();

		if(StringUtils.isBlank(id)){
			throw new Exception("id 값을 설정해주세요");
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

	public String toJson(TagFormParams params) throws Exception {

		String jsonString = mapper.writeValueAsString(params);
		return jsonString;
	}

	public String delete(SentenceFormParams params){

		String index = params.getIndex();
		String id = params.getId();

        DeleteResponse response = client.prepareDelete(INDEX, TYPE, id)
//                .setOperationThreaded(false)
                .get();

		return response.toString();

    }

    private String createId(){
		return UUIDs.base64UUID();
	}


}