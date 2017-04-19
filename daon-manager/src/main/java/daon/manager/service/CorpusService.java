package daon.manager.service;

import daon.analysis.ko.model.CandidateTerm;
import daon.manager.model.CorpusParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FiltersFunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.fieldValueFactorFunction;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class CorpusService {

	@Autowired
	private TransportClient esClient;


	public SearchResponse search(CorpusParams params) throws IOException {

		BoolQueryBuilder boolQueryBuilder = boolQuery();

		List<String> seqs = params.getSeq();

		if(seqs != null){
			for(String seq : seqs) {
				boolQueryBuilder.must(matchQuery("word_seqs", seq));
			}
		}

		SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch("corpus")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(
					boolQueryBuilder
				)
				.setFrom(params.getFrom())
				.setSize(params.getSize())
				;

//		if(filter.isRecent()) {
//			searchRequestBuilder.addSort("udate", SortOrder.DESC);
//		}

//		if(filter.isDebug()) {
//			searchRequestBuilder.setExplain(true).setProfile(true); // Debug mode
//		}

		SearchResponse response = searchRequestBuilder
				.execute()
				.actionGet();

//		List<Content> results = contentsResponse.getContent();
//
//		response.getHits().forEach( hit -> {
//			Map<String, Object> source = hit.getSource();
//			source.put("score", hit.getScore());
//
//			if(filter.isDebug()) {
//				// Debug mode
//				Explanation explanation = hit.getExplanation();
//				source.put("explanation", explanation);
//			}
//
//			results.add(new Content(source));
//		});
//
//		// Debug mode
//		if(filter.isDebug()) {
//			log.info("query => \n{}", searchRequestBuilder);
//			contentsResponse.setProfileResults(response.getProfileResults());
//		}
//
//		contentsResponse.setNumberOfElements(response.getHits().hits().length);
//		contentsResponse.setTotalElements(response.getHits().totalHits());
//		contentsResponse.calculateTotalPages();
//
//		contentsResponse.setTerms(analyzedTerms);



		return response;
	}

}