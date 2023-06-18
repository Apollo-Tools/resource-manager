package org.apollorm.jsonhandling;

import org.apollorm.Input;

/**
 * This class represents the http request and is used for deserialization of the request. Do not
 * modify.
 *
 * @author matthi-g
 */
public class Request {
    private Input input;

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }
}
