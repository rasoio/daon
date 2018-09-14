package org.elasticsearch.rest;

import org.elasticsearch.action.DaonModelAction;
import org.elasticsearch.action.DaonModelRequestBuilder;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.action.RestActions;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.DeprecationHandler.THROW_UNSUPPORTED_OPERATION;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestDaonModel extends BaseRestHandler {

    public static class Fields {
        public static final ParseField INIT = new ParseField("init");

        public static final ParseField FILE_PATH = new ParseField("filePath");
        public static final ParseField URL = new ParseField("url");
        public static final ParseField TIMEOUT = new ParseField("timeout");
    }

    public RestDaonModel(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/_daon_model", this);
        controller.registerHandler(GET, "/_daon_model", this);
    }

    @Override
    public String getName() {
        return "daon_model_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {

        final DaonModelRequestBuilder requestBuilder = new DaonModelRequestBuilder(client, DaonModelAction.INSTANCE);

        requestBuilder.setInit(request.paramAsBoolean("init", false));
        requestBuilder.setFilePath(request.param("filePath"));
        requestBuilder.setURL(request.param("url"));
        requestBuilder.setTimeout(request.paramAsLong("timeout", 30000));

        try {
            if(request.hasContentOrSourceParam()) {
                handleBodyContent(request, requestBuilder);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse request body", e);
        }

        return channel -> client.execute(DaonModelAction.INSTANCE, requestBuilder.request(),
                new RestActions.NodesResponseRestListener<>(channel));
    }

    private void handleBodyContent(RestRequest request, DaonModelRequestBuilder requestBuilder) throws IOException {
        XContentParser parser = request.contentOrSourceParamParser();
        // NOTE: if rest request with xcontent body has request parameters, the parameters do not override xcontent values
        buildFromContent(parser, requestBuilder);
    }

    private void buildFromContent(XContentParser parser, DaonModelRequestBuilder requestBuilder) throws IOException {
        if (parser.nextToken() != XContentParser.Token.START_OBJECT) {
            throw new IllegalArgumentException("Malformed content, must start with an object");
        } else {
            XContentParser.Token token;
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (Fields.INIT.match(currentFieldName, THROW_UNSUPPORTED_OPERATION) && token == XContentParser.Token.VALUE_BOOLEAN) {
                    requestBuilder.setInit(parser.booleanValue());
                } else if (Fields.FILE_PATH.match(currentFieldName, THROW_UNSUPPORTED_OPERATION) && token == XContentParser.Token.VALUE_STRING) {
                    requestBuilder.setFilePath(parser.text());
                } else if (Fields.URL.match(currentFieldName, THROW_UNSUPPORTED_OPERATION) && token == XContentParser.Token.VALUE_STRING) {
                    requestBuilder.setURL(parser.text());
                } else if (Fields.TIMEOUT.match(currentFieldName, THROW_UNSUPPORTED_OPERATION)) {
                    if (token == XContentParser.Token.VALUE_STRING) {
                        TimeValue timeValue = TimeValue.parseTimeValue(parser.text(), null, currentFieldName);
                        requestBuilder.setTimeout(timeValue.millis());
                    } else {
                        requestBuilder.setTimeout(parser.longValue());
                    }
                } else {
                    throw new IllegalArgumentException("Unknown parameter ["
                            + currentFieldName + "] in request body or parameter is of the wrong type[" + token + "] ");
                }
            }
        }
    }
}
