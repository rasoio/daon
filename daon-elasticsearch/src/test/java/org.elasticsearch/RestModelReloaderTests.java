package org.elasticsearch;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.rest.ESRestTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class RestModelReloaderTests extends ESRestTestCase {

    public void testCallReload() throws IOException {

        logger.info("시작 했네????~~~~~~~!!!!!!!!!");

        RestClient client = client();
        Response response = client.performRequest("GET", "_cat/plugins");


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

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
