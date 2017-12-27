package daon.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import daon.core.config.POSTag;
import daon.manager.model.param.SentenceFormParams;
import daon.manager.model.param.TagParams;
import daon.manager.model.param.WordParams;
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

	public Map<String, Object> get(TagParams params) {
	    String index = params.getIndex();

		GetResponse response = client.prepareGet(index, TYPE, params.getId()).get();

		return response.getSourceAsMap();
	}

	public String search(TagParams params) throws IOException {

		BoolQueryBuilder mainQueryBuilder = boolQuery();

		List<String> positions = params.getPosition();

		if(positions != null && positions.size() > 0){
			mainQueryBuilder.must(termsQuery("position", positions));
        }

		String tag = params.getTag();

		if(StringUtils.isNoneBlank(tag)){
			mainQueryBuilder.should(matchQuery("tag1", tag));
			mainQueryBuilder.should(matchQuery("tag2", tag));
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