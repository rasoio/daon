package daon.manager.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Created by mac on 2017. 3. 6..
 */
@Slf4j
@Component
public class ElasticsearchClient {

	@Value("${es.clusterNodes}")
	private String clusterNodes;

	@Value("${es.cluster}")
	private String cluster;

	@Bean(name="esClient", destroyMethod="close")
	public TransportClient connection() throws Exception {
		Settings settings = Settings.builder().put("cluster.name", cluster).build();

		TransportClient client = new PreBuiltTransportClient(settings);

		try {
			for(String clusterNode : clusterNodes.split(",")) {
				String[] info = clusterNode.split(":");

				String ip = info[0];
				int port = NumberUtils.toInt(info[1], 9300);

				client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
			}
		} catch (Exception e) {
			log.error("# host => {} addTransportAddress exception.", clusterNodes, e);
			throw e;
		}

		return client;
	}

}