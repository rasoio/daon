/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action;

import daon.core.model.ModelInfo;
import daon.core.util.ModelUtils;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.nodes.BaseNodeRequest;
import org.elasticsearch.action.support.nodes.TransportNodesAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.NodeService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.List;

public class TransportDaonModelAction extends TransportNodesAction<DaonModelRequest, DaonModelResponse,
        TransportDaonModelAction.NodeRequest, DaonModelStats> {

    private static final int DEFAULT_TIMEOUT = 30000;

    private final ClusterService clusterService;

    @Inject
    public TransportDaonModelAction(Settings settings, ThreadPool threadPool,
                                     ClusterService clusterService, TransportService transportService,
                                     NodeService nodeService, ActionFilters actionFilters,
                                     IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, DaonModelAction.NAME, threadPool, clusterService, transportService, actionFilters,
                indexNameExpressionResolver, DaonModelRequest::new, NodeRequest::new, ThreadPool.Names.MANAGEMENT, DaonModelStats.class);


        this.clusterService = clusterService;
    }

    @Override
    protected DaonModelResponse newResponse(DaonModelRequest request, List<DaonModelStats> responses, List<FailedNodeException> failures) {
        return new DaonModelResponse(clusterService.getClusterName(), responses, failures);
    }

    @Override
    protected NodeRequest newNodeRequest(String nodeId, DaonModelRequest request) {
        return new NodeRequest(nodeId, request);
    }

    @Override
    protected DaonModelStats newNodeResponse() {
        return new DaonModelStats();
    }

    @Override
    protected DaonModelStats nodeOperation(NodeRequest nodeStatsRequest) {
        DaonModelRequest request = nodeStatsRequest.request;

        String filePath = request.getFilePath();
        String url = request.getUrl();
        TimeValue timeValue = request.timeout();
        int timeout = DEFAULT_TIMEOUT;

        if(timeValue != null){
            timeout = (int) timeValue.millis();
        }

        ModelInfo modelInfo;

        if(filePath != null) {
            logger.info("file read");
            modelInfo = ModelUtils.loadModelByFile(filePath);
        }else if(url != null){
            logger.info("url read");
            modelInfo = ModelUtils.loadModelByURL(url, timeout);
        }else{
            //current modelInfo
            modelInfo = ModelUtils.getModel();
        }

        if(modelInfo.isSuccess()) {
            ModelUtils.setModel(modelInfo);
        }

        DiscoveryNode node = clusterService.localNode();

        return new DaonModelStats(node, modelInfo);
    }



    public static class NodeRequest extends BaseNodeRequest {

        DaonModelRequest request;

        public NodeRequest() {
        }

        NodeRequest(String nodeId, DaonModelRequest request) {
            super(nodeId);
            this.request = request;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            request = new DaonModelRequest();
            request.readFrom(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            request.writeTo(out);
        }
    }
}
