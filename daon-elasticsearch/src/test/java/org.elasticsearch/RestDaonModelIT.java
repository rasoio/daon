package org.elasticsearch;

import org.apache.http.StatusLine;
import org.elasticsearch.action.DaonModelAction;
import org.elasticsearch.action.DaonModelRequestBuilder;
import org.elasticsearch.action.DaonModelResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsAction;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.plugin.analysis.daon.AnalysisDaonPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ESIntegTestCase.ClusterScope(minNumDataNodes = 0, maxNumDataNodes = 5,
        scope = ESIntegTestCase.Scope.SUITE, numClientNodes = 1, transportClientRatio = 0.0)
public class RestDaonModelIT extends ESIntegTestCase {

    public void testCallReload() throws IOException {

        RestClient client = getRestClient();

        Map<String, String> params = new HashMap<>();
//        params.put("filePath", "/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat");
//        params.put("url", "file:////Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat");
//        params.put("url", "http://search-dev02:5001/v1/model/download?seq=1505445566914");
        Response response = client.performRequest("GET", "/_daon_model?pretty=true", params);


        StatusLine status = response.getStatusLine();


//        Map<String, Object> data = entityAsMap(response);

        logger.info("response : {}", getStringFromInputStream(response.getEntity().getContent()));
//        ClusteringActionResponse result = new ClusteringActionRequestBuilder(client)
//                .setQueryHint("data mining")
//                .addSourceFieldMapping("title", LogicalField.TITLE)
//                .addHighlightedFieldMapping("content", LogicalField.CONTENT)
//                .setSearchRequest(
//                        client.prepareSearch()
//                                .setIndices(INDEX_TEST)
//                                .setTypes("test")
//                                .setSize(100)
//                                .setQuery(QueryBuilders.termQuery("content", "data"))
//                                .highlighter(new HighlightBuilder().preTags("").postTags(""))
//                                .setFetchSource(new String[] {"title"}, null)
//                                .highlighter(new HighlightBuilder().field("content")))
//                .execute().actionGet();
//
//        checkValid(result);
//        checkJsonSerialization(result);
    }

    @Override
    protected Collection<Class<? extends Plugin>> transportClientPlugins() {
        return Collections.<Class<? extends Plugin>> singletonList(AnalysisDaonPlugin.class);
    }

    public void testActionRequest() throws IOException {

        Client client = client();

        logger.info("client : {}", client);

        DaonModelResponse result = new DaonModelRequestBuilder(client, DaonModelAction.INSTANCE)
//                .setFilePath("/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat")
//                .setURL("http://localhost:5001/v1/model/download?seq=1504775190639")
                .setURL("http://search-dev02:5001/v1/model/download?seq=1505445566914")
//                .get(new TimeValue(30000));
                .execute().actionGet();
//                .execute().actionGet();

//        NodesStatsResponse result = new NodesStatsRequestBuilder(client(), NodesStatsAction.INSTANCE).all().execute().actionGet();

        String json = result.toString();

        logger.info("response : {}", json);
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
