package at.uibk.dps.rm.entity.dto.slo;

public class SLOValue {
    private SLOValueType sloValueType;

    private Number valueNumber;

    private String valuerString;

    private boolean valueBool;

    public SLOValue() {
    }

    public SLOValueType getSloValueType() {
        return sloValueType;
    }

    public void setSloValueType(SLOValueType sloValueType) {
        this.sloValueType = sloValueType;
    }

    public Number getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(Number valueNumber) {
        this.valueNumber = valueNumber;
    }

    public String getValuerString() {
        return valuerString;
    }

    public void setValuerString(String valuerString) {
        this.valuerString = valuerString;
    }

    public boolean getValueBool() {
        return valueBool;
    }

    public void setValueBool(boolean valueBool) {
        this.valueBool = valueBool;
    }
}
