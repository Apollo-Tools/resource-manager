package org.apollorm.model;

import java.util.Arrays;

/**
 * The request type is used to determine structure of the response body. If the request type is
 * equal to 'rm', additional measurement data is added to the response body.
 *
 * @author matthi-g
 */
public enum RequestType {
    /**
     * rm
     */
    RM("rm"),
    /**
     * client
     */
    CLIENT("client");

    private final String value;

    RequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Create an instance from a string value. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param requestType the request type
     * @return the created object
     */
    public static RequestType fromString(String requestType) {
        return Arrays.stream(RequestType.values())
            .filter(value -> value.value.equals(requestType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown value: " + requestType));
    }
}
