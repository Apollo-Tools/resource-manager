package org.apollorm.entrypoint;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

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
        if (node.has("body")) {
            body = node.get("body").asText().replace("\\", "\"");
        } else {
            body = node.toString();
        }
        Request request = new Request();
        request.setBody(body);
        return request;
    }
}
