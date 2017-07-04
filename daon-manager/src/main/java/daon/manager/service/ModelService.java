package daon.manager.service;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.reader.ModelReader;
import daon.manager.config.DaonConfig;
import daon.manager.model.ModelParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.lang.String.*;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class ModelService {

    @Value("${spark.home}")
    private String home;

    @Value("${spark.appResource}")
    private String appResource;

    @Value("${spark.master}")
    private String master;

    @Autowired
    private TransportClient client;

    private static String INDEX = "models";
    private static String TYPE = "model";

    private SparkAppHandle sparkAppHandle;

    public SparkAppHandle.State make() throws IOException {

        if(sparkAppHandle == null || sparkAppHandle.getState().isFinal()) {

            sparkAppHandle = new SparkLauncher()
                    .setSparkHome(home)
                    .setAppResource(appResource)
                    .setMainClass("daon.dictionary.spark.MakeModel")
                    .setMaster(master)
                    .setConf(SparkLauncher.DRIVER_MEMORY, "8g")
//                .setConf(SparkLauncher.EXECUTOR_MEMORY, "8g")
                    .startApplication();

            sparkAppHandle.addListener(new SparkAppHandle.Listener() {
                @Override
                public void stateChanged(SparkAppHandle handle) {

                }

                @Override
                public void infoChanged(SparkAppHandle handle) {

                }
            });
        }

        return state();
    }


    public SparkAppHandle.State state() {
        return sparkAppHandle.getState();
    }

    public byte[] model(String seq){
        GetResponse response = client.prepareGet(INDEX, TYPE, seq).setStoredFields("data").get();

        BytesArray bytesArray = (BytesArray) response.getField("data").getValue();

        return bytesArray.array();
    }

    private String maxSeq(){

        MaxAggregationBuilder aggregation =
                AggregationBuilders
                        .max("max_seq")
                        .field("seq");

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(0)
                .setSize(0)
                .addAggregation(aggregation);


        log.info("max query : {}", searchRequestBuilder);

        SearchResponse response = searchRequestBuilder
                .execute()
                .actionGet();

        Max agg = response.getAggregations().get("max_seq");

        long value = ((long) agg.getValue());

        return valueOf(value);
    }


    public ModelInfo defaultModelInfo() throws IOException {

        ModelInfo modelInfo = ModelReader.create().load();

        return modelInfo;
    }

    public ModelInfo modelInfo() throws IOException {
        String seq = maxSeq();

        byte[] data = model(seq);

        InputStream inputStream = new ByteArrayInputStream(data);

        ModelInfo modelInfo = ModelReader.create().inputStream(inputStream).load();

        return modelInfo;
    }

    public SearchResponse search(ModelParams params) {

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

        return response;
    }
}