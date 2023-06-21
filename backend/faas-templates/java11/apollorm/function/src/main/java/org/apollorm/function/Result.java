package org.apollorm.function;

/**
 * This class represents the result for the function. Modify it to your  requirements.
 * To be serializable, all fields must have a getter and setter. If a field type is a custom type
 * these rules also apply for the custom type.
 *
 * @author matthi-g
 */
public class Result {

    private int result;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
