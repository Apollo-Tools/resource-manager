package org.apollorm.entrypoint;

import org.apollorm.model.RequestType;

/**
 * Represents the request object.
 *
 * @author matthi-g
 */
public class Request {
    private String body;

    private RequestType requestType;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }
}
