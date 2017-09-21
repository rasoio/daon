package org.elasticsearch.debug;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DaonModelRequestBuilder;
import org.elasticsearch.action.DaonModelResponse;
import org.elasticsearch.action.DaonModelStats;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.MockNode;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugin.analysis.daon.AnalysisDaonPlugin;
import org.elasticsearch.plugins.Plugin;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Collections;


@RunWith(RandomizedRunner.class)
public class StartNode {

    private final Logger logger = Loggers.getLogger(getClass());

    @Test
    public void testStartNode() throws Exception {
        Settings settings = Settings.builder() // All these are required or MockNode will fail to build.
                .put("cluster.name", "test-cluster")
                .put("path.home", "target/home")
                .put("path.data", "target/data")
                .put("path.conf", "target/conf")

                .put("transport.type", "local")
                .put("http.enabled", false)
                .build();

        Collection<Class<? extends Plugin>> plugins = Collections.<Class<? extends Plugin>> singletonList(AnalysisDaonPlugin.class);

        try (Node node = new MockNode(settings, plugins)) {
            node.start();

            Client client = node.client();


//            NodesStatsResponse response = new NodesStatsRequestBuilder(client, NodesStatsAction.INSTANCE).execute().actionGet();
//            System.out.println(response);


            DaonModelResponse response = new DaonModelRequestBuilder(client)
//                    .setFilePath("/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat")
//                    .setFilePath("/Users/mac/work/daon/daon-core/src/main/resources/daon/core/reader/model.dat")
//                .setURL("http://localhost:5001/v1/model/download?seq=1504775190639")
//                .setURL("http://search-dev02:5001/v1/model/download?seq=1505445566914")
                    .execute().actionGet();

            logger.info("response : {}", response);

            for(DaonModelStats daonModelStats : response.getNodes()) {
                logger.info("hostname : {}, success : {}, target : {}, dictionarySize : {}, loadedDate : {}, elapsed : {}",
                        daonModelStats.getHostname(), daonModelStats.isSuccess(), daonModelStats.getTarget(),
                        daonModelStats.getDictionarySize(), daonModelStats.getLoadedDate(), daonModelStats.getElapsed());
            }

//            new CountDownLatch(1).await();
        }

    }

}
