package at.uibk.dps.rm.entity.dto.slo;

public class SLOValue {
    private SLOValueType sloValueType;

    private Number numberValue;

    private String stringValue;

    private boolean booleanValue;

    public SLOValue() {
    }

    public SLOValueType getSloValueType() {
        return sloValueType;
    }

    public void setSloValueType(SLOValueType sloValueType) {
        this.sloValueType = sloValueType;
    }

    public Number getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Number numberValue) {
        this.numberValue = numberValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
