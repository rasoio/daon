package org.elasticsearch.debug;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DaonModelAction;
import org.elasticsearch.action.DaonModelRequestBuilder;
import org.elasticsearch.action.DaonModelResponse;
import org.elasticsearch.action.DaonModelStats;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.MockNode;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugin.analysis.daon.AnalysisDaonPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.transport.MockTransportService;
import org.elasticsearch.transport.MockTcpTransportPlugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@RunWith(RandomizedRunner.class)
public class StartNode {

    private final Logger logger = Loggers.getLogger(getClass());

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
//        List<Class<? extends Plugin>> plugins = new ArrayList<>();
//        plugins.add(AnalysisDaonPlugin.class);
//        plugins.add(MockTcpTransportPlugin.class);
//        plugins.add(MockTransportService.TestPlugin.class);

        try (Node node = new MockNode(settings, plugins)) {
            node.start();

            Client client = node.client();

//            NodesStatsResponse response = new NodesStatsRequestBuilder(client, NodesStatsAction.INSTANCE).execute().actionGet();
//            System.out.println(response);

            DaonModelResponse response = new DaonModelRequestBuilder(client, DaonModelAction.INSTANCE)
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

    public void testExternalClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();

        Collection<Class<? extends Plugin>> plugins = Collections.<Class<? extends Plugin>> singletonList(AnalysisDaonPlugin.class);

        TransportClient client = new PreBuiltTransportClient(settings, plugins);

        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

        DaonModelResponse response = new DaonModelRequestBuilder(client, DaonModelAction.INSTANCE)
//                    .setFilePath("/Users/mac/IdeaProjects/daon/daon-core/src/main/resources/daon/core/reader/model.dat")
//                    .setFilePath("/Users/mac/work/daon/daon-core/src/main/resources/daon/core/reader/model.dat")
//                .setURL("http://localhost:5001/v1/model/download?seq=1504775190639")
//                .setURL("http://search-dev02:5001/v1/model/download?seq=1505445566914")
                .execute().actionGet();

        logger.info("response : {}", response);
    }
    public static String getTestTransportType() {
        return MockTcpTransportPlugin.MOCK_TCP_TRANSPORT_NAME;
    }
}
