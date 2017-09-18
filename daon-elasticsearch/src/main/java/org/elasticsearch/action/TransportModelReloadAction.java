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
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.TransportSearchAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.action.support.master.TransportMasterNodeReadAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateObserver;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.LocalClusterUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.gateway.GatewayAllocator;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.tasks.TaskManager;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportRequestHandler;
import org.elasticsearch.transport.TransportService;

import java.util.function.Predicate;

public class TransportModelReloadAction extends TransportAction<ModelReloadRequest, ModelReloadResponse> {

    @Inject
    public TransportModelReloadAction(Settings settings,
                                     ThreadPool threadPool,
                                     TransportService transportService,
                                     ActionFilters actionFilters,
                                     IndexNameExpressionResolver indexNameExpressionResolver,
                                     NamedXContentRegistry xContentRegistry) {
        super(settings,
                ModelReloadAction.NAME,
                threadPool,
                actionFilters,
                indexNameExpressionResolver,
                transportService.getTaskManager());

        transportService.registerRequestHandler(
                ModelReloadAction.NAME,
                ModelReloadRequest::new,
                ThreadPool.Names.SAME,
                new TransportHandler());
    }

    @Override
    protected void doExecute(ModelReloadRequest request, ActionListener<ModelReloadResponse> listener) {

        String filePath = request.getFilePath();
        String url = request.getUrl();

        boolean isSuccess = false;
        String message = "";

        ModelInfo modelInfo = null;
        if(filePath != null) {
            modelInfo = ModelUtils.loadModelByFile(filePath);
        }

        if(url != null){
            modelInfo = ModelUtils.loadModelByURL(url);
        }

        ModelUtils.setModel(modelInfo);

        listener.onResponse(new ModelReloadResponse(isSuccess, message));

        if(modelInfo == null) {
            message = "Search results clustering error: ";
            listener.onFailure(new ElasticsearchException(message));

            logger.warn("Could not process model reload request.");
        }

    }


    private final class TransportHandler implements TransportRequestHandler<ModelReloadRequest> {
        @Override
        public void messageReceived(final ModelReloadRequest request, final TransportChannel channel) throws Exception {
            execute(request, new ActionListener<ModelReloadResponse>() {
                @Override
                public void onResponse(ModelReloadResponse response) {
                    try {
                        channel.sendResponse(response);
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    try {
                        channel.sendResponse(e);
                    } catch (Exception e1) {
                        logger.warn("Failed to send error response for action ["
                                + ModelReloadAction.NAME + "] and request [" + request + "]", e1);
                    }
                }
            });
        }
    }
}
