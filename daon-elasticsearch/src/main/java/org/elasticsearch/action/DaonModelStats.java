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
import org.elasticsearch.action.support.nodes.BaseNodeResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Daon model statistics (dynamic, changes depending on when created).
 */
public class DaonModelStats extends BaseNodeResponse implements ToXContent {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.KOREA);

    private boolean isSuccess;
    private String target;
    private int dictionarySize;
    private String loadedDate;
    private long elapsed;
    private String errorMsg;

    DaonModelStats() {}

    public DaonModelStats(DiscoveryNode node, ModelInfo modelInfo) {
        super(node);

        this.isSuccess = modelInfo.isSuccess();
        this.target = modelInfo.getTarget();
        this.dictionarySize = modelInfo.getDictionarySize();
        Date loadedDate = modelInfo.getLoadedDate();
        if(loadedDate != null) {
            this.loadedDate = dateFormat.format(modelInfo.getLoadedDate());
        }
        this.elapsed = modelInfo.getElapsed();
        this.errorMsg = modelInfo.getErrorMsg();

    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getTarget() {
        return target;
    }

    public int getDictionarySize() {
        return dictionarySize;
    }

    public String getLoadedDate() {
        return loadedDate;
    }

    public long getElapsed() {
        return elapsed;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Nullable
    public String getHostname() {
        return getNode().getHostName();
    }

    public static DaonModelStats readNodeStats(StreamInput in) throws IOException {
        DaonModelStats nodeInfo = new DaonModelStats();
        nodeInfo.readFrom(in);
        return nodeInfo;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        isSuccess = in.readBoolean();
        target = in.readOptionalString();
        dictionarySize = in.readVInt();
        loadedDate = in.readOptionalString();
        elapsed = in.readVLong();
        errorMsg = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBoolean(isSuccess);
        out.writeString(target);
        out.writeVInt(dictionarySize);
        out.writeString(loadedDate);
        out.writeVLong(elapsed);
        out.writeString(errorMsg);

    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {

        builder.field("name", getNode().getName());
        builder.field("transport_address", getNode().getAddress().toString());
        builder.field("host", getNode().getHostName());
        builder.field("ip", getNode().getAddress());

        builder.startArray("roles");
        for (DiscoveryNode.Role role : getNode().getRoles()) {
            builder.value(role.getRoleName());
        }
        builder.endArray();

        if (!getNode().getAttributes().isEmpty()) {
            builder.startObject("attributes");
            for (Map.Entry<String, String> attrEntry : getNode().getAttributes().entrySet()) {
                builder.field(attrEntry.getKey(), attrEntry.getValue());
            }
            builder.endObject();
        }

        builder.field("success", isSuccess);
        builder.field("target", target);

        if(isSuccess) {
            builder.field("dictionarySize", dictionarySize);
            builder.field("loadedDate", loadedDate);
            builder.field("elapsed", elapsed);
        }else {
            builder.field("errorMsg", errorMsg);
        }

        return builder;
    }
}
