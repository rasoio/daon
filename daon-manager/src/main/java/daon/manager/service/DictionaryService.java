package daon.manager.service;

import daon.manager.model.DictionaryParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class DictionaryService {

	@Autowired
	private TransportClient client;


	public Map<String, Object> get(DictionaryParams params) {
		GetResponse response = client.prepareGet("dictionary", "words", params.getId()).get();

		return response.getSourceAsMap();
	}

	public SearchResponse search(DictionaryParams params) throws IOException {

		BoolQueryBuilder boolQueryBuilder = boolQuery();

		String keyword = params.getKeyword();

		if(keyword != null){
            boolQueryBuilder.filter(wildcardQuery("word", "*" + keyword + "*"));
        }

		String tag = params.getTag();

		if(StringUtils.isNotEmpty(tag)){
			boolQueryBuilder.filter(matchQuery("tag", tag));
		}

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch("dictionary")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQueryBuilder)
				.setFrom(params.getFrom())
				.setSize(params.getSize());

		log.info("query : {}", searchRequestBuilder);

//		if(filter.isDebug()) {
//			searchRequestBuilder.setExplain(true).setProfile(true); // Debug mode
//		}

		SearchResponse response = searchRequestBuilder
				.execute()
				.actionGet();


		return response;
	}

}