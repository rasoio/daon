package daon.manager.service;

import daon.analysis.ko.model.MatchInfo;
import daon.manager.model.CorpusParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

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

		List<Integer> seqs = params.getSeq();

		if(seqs != null){
			for(Integer seq : seqs) {
				boolQueryBuilder.must(matchQuery("word_seqs", seq));
			}
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


		return response;
	}

}