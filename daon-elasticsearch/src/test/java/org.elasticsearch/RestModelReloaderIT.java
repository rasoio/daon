package org.elasticsearch;

import org.apache.http.StatusLine;
import org.elasticsearch.action.ModelReloadRequestBuilder;
import org.elasticsearch.action.ModelReloadResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.rest.ESRestTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RestModelReloaderIT extends ESIntegTestCase {

    public void testCallReload() throws IOException {

        logger.info("시작 했네????~~~~~~~!!!!!!!!!");

        RestClient client = getRestClient();

        Map<String, String> params = new HashMap<>();
//        params.put("filePath", "/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat");
//        params.put("url", "file:////Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat");
        params.put("url", "http://search-dev02:5001/v1/model/download?seq=1505445566914");
        Response response = client.performRequest("GET", "/_model_reload?pretty=true", params);


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

    public void testActionRequest() throws IOException {
        ModelReloadResponse result = new ModelReloadRequestBuilder(client())
//                .setFilePath("/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat")
//                .setURL("http://localhost:5001/v1/model/download?seq=1504775190639")
                .setURL("http://search-dev02:5001/v1/model/download?seq=1505445566914")
                .execute().actionGet();


        XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
        builder.startObject();
        result.toXContent(builder, ToXContent.EMPTY_PARAMS);
        builder.endObject();
        String json = builder.string();

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
