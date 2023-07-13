package org.apollorm.function;

import java.util.List;
public class Result {

    private double inputSizeMb;

    private List<String> splitted;

    private Double actualMs;

    public double getInputSizeMb() {
        return inputSizeMb;
    }

    public void setInputSizeMb(double inputSizeMb) {
        this.inputSizeMb = inputSizeMb;
    }

    public List<String> getSplitted() {
        return splitted;
    }

    public void setSplitted(List<String> splitted) {
        this.splitted = splitted;
    }

    public Double getActualMs() {
        return actualMs;
    }

    public void setActualMs(Double actualMs) {
        this.actualMs = actualMs;
    }
}
