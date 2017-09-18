package org.elasticsearch.rest;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ModelReloadAction;
import org.elasticsearch.action.ModelReloadRequestBuilder;
import org.elasticsearch.action.ModelReloadResponse;
import org.elasticsearch.action.main.MainAction;
import org.elasticsearch.action.main.MainRequest;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.action.RestBuilderListener;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.PUT;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestModelReloader extends BaseRestHandler {

    public RestModelReloader(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/_model_reload", this);
        controller.registerHandler(GET, "/_model_reload", this);
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {

        // A POST request must have a body.
        if (request.method() == POST && !request.hasContent()) {
            return channel -> emitErrorResponse(channel, logger,
                    new IllegalArgumentException("Request body was expected for a POST request."));
        }

        // Build an action request with data from the request.

        // Parse incoming arguments depending on the HTTP method used to make
        // the request.
        final ModelReloadRequestBuilder actionBuilder = new ModelReloadRequestBuilder(client);

        switch (request.method()) {
            case POST:
                actionBuilder.setFilePath(request.param("filePath"));
                actionBuilder.setURL(request.param("url"));

                break;

            case GET:
                actionBuilder.setFilePath(request.param("filePath"));
                actionBuilder.setURL(request.param("url"));
                break;

            default:
                throw new RuntimeException("Unreachable code assertion hit.");
        }

        return channel -> client.execute(ModelReloadAction.INSTANCE, actionBuilder.request(),
                new RestBuilderListener<ModelReloadResponse>(channel) {
            @Override
            public RestResponse buildResponse(ModelReloadResponse modelReloadResponse, XContentBuilder builder) throws Exception {
                return convertMainResponse(modelReloadResponse, request, builder);
            }
        });
    }

    static BytesRestResponse convertMainResponse(ModelReloadResponse response, RestRequest request, XContentBuilder builder)
            throws IOException {
        RestStatus status = response.isSuccess() ? RestStatus.OK : RestStatus.SERVICE_UNAVAILABLE;

        // Default to pretty printing, but allow ?pretty=false to disable
        if (request.hasParam("pretty") == false) {
            builder.prettyPrint().lfAtEnd();
        }
        response.toXContent(builder, request);
        return new BytesRestResponse(status, builder);
    }

    static void emitErrorResponse(RestChannel channel,
                                  Logger logger,
                                  Exception e) {
        try {
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (IOException e1) {
            logger.error("Failed to send failure response.", e1);
        }
    }
}
