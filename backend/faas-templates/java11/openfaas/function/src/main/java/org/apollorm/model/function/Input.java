package org.apollorm.model.function;

/**
 * This class represents the input for the function. Modify it to your  requirements.
 * To be deserializable, all fields must have a getter and setter. If a field type is a custom type
 * these rules also apply for the custom type.
 *
 * @author matthi-g
 */
public class Input {

    private int input1;

    public int getInput1() {
        return input1;
    }

    public void setInput1(int input1) {
        this.input1 = input1;
    }
}
