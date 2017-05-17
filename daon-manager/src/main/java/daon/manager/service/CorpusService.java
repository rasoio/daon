package daon.manager.service;

import daon.analysis.ko.model.MatchInfo;
import daon.manager.model.CorpusParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

	private static String INDEX = "corpus";
	private static String TYPE = "sentences";


	public Map<String, Object> get(CorpusParams params) {
		GetResponse response = client.prepareGet(INDEX, TYPE, params.getId()).get();

		return response.getSourceAsMap();
	}

	public SearchResponse search(CorpusParams params) throws IOException {

		BoolQueryBuilder boolQueryBuilder = boolQuery();

		List<Integer> seqs = params.getSeq();

		if(seqs != null){
			for(Integer seq : seqs) {
				boolQueryBuilder.filter(matchQuery("word_seqs", seq));
			}
		}

		String keyword = params.getKeyword();

		//TODO performance issue
		if(keyword != null){
            boolQueryBuilder.filter(wildcardQuery("sentence", "*" + keyword + "*"));
        }


		MatchInfo matchInfo = params.getMatchInfo();

		if(matchInfo != null){
			MatchInfo.MatchType type = matchInfo.getType();

			switch (type) {
				case DICTIONARY:
					int[] matchSeqs = matchInfo.getSeqs();

					for(Integer seq : matchSeqs) {
						boolQueryBuilder.must(matchQuery("word_seqs", seq));
					}

					break;
				case PREV_CONNECTION:

					String prevFieldName = "eojeols.morphemes.p_inner_seq";

					if(matchInfo.isOuter()){
						prevFieldName = "eojeols.morphemes.p_outer_seq";
					}

					boolQueryBuilder.must(nestedQuery("eojeols.morphemes",
						boolQuery()
								.must(matchQuery(prevFieldName, matchInfo.getPrevSeq()))
								.must(matchQuery("eojeols.morphemes.seq", matchInfo.getSeq()))
					, ScoreMode.None));

					break;
				case NEXT_CONNECTION:

					boolQueryBuilder.must(nestedQuery("eojeols.morphemes",
						boolQuery()
								.must(matchQuery("eojeols.morphemes.seq", matchInfo.getSeq()))
								.must(matchQuery("eojeols.morphemes.n_inner_seq", matchInfo.getNextSeq()))
					, ScoreMode.None));

					break;
			}
		}

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQueryBuilder)
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


		return response;
	}




	public void delete(){

	    String id = "";
        DeleteResponse response = client.prepareDelete(INDEX, TYPE, id)
//                .setOperationThreaded(false)
                .get();


    }


    public void upsert() throws IOException, ExecutionException, InterruptedException {

	    String jsonStr = "";
	    String id = "";

        IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, id)
                .source(jsonStr);
        UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, id)
                .doc(jsonStr)
                .upsert(indexRequest);

        client.update(updateRequest).get();

    }


}