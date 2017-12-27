package daon.manager.service;

import daon.core.result.ModelInfo;
import daon.core.util.ModelUtils;
import daon.manager.model.param.ModelParams;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.valueOf;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class ModelService {

    @Autowired
    private TransportClient client;

    private static String INDEX = "models";
    private static String TYPE = "model";


    public byte[] model(String seq){
        GetResponse response = client.prepareGet(INDEX, TYPE, seq).setStoredFields("data").get();

        BytesArray bytesArray = (BytesArray) response.getField("data").getValue();

        return bytesArray.array();
    }

    public ModelInfo modelInfo(String seq) throws IOException {

        byte[] data = model(seq);

        InputStream inputStream = new ByteArrayInputStream(data);

        ModelInfo modelInfo = ModelUtils.loadModelByInputStream(inputStream);

        return modelInfo;
    }

    public String search(ModelParams params) {

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(params.getFrom())
                .setSize(params.getSize())
                .addSort("seq", SortOrder.DESC);

        log.info("query : {}", searchRequestBuilder);

//		if(filter.isDebug()) {
//			searchRequestBuilder.setExplain(true).setProfile(true); // Debug mode
//		}

        SearchResponse response = searchRequestBuilder
                .execute()
                .actionGet();

        return response.toString();
    }
}