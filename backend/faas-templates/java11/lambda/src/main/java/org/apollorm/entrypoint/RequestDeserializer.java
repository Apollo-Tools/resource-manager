package org.apollorm.entrypoint;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apollorm.model.RequestType;

import java.io.IOException;

/**
 * This class can be used to deserialize a {@link Request}.
 *
 * @author matthi-g
 */
public class RequestDeserializer extends StdDeserializer<Request> {

    public RequestDeserializer() {
        this(null);
    }

    public RequestDeserializer(Class<?> vc) {
        super(vc);

    }

    @Override
    public Request deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String body;
        RequestType requestType = RequestType.CLIENT;
        // parse request body
        if (node.has("body")) {
            body = node.get("body").asText().replace("\\\\", "\\");
        } else {
            body = node.toString();
        }
        // parse request headers
        if (node.has("headers") && node.get("headers").has("apollo-request-type")) {
            try {
                requestType = RequestType.fromString(node.get("headers").get("apollo-request-type").asText());
            } catch (IllegalArgumentException ex) {
                requestType = RequestType.CLIENT;
            }
        }
        Request request = new Request();
        request.setBody(body);
        request.setRequestType(requestType);
        return request;
    }
}
