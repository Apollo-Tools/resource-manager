package at.uibk.dps.rm.entity.dto.slo;

import lombok.Data;

@Data
public class SLOValue {
    private SLOValueType sloValueType;

    private Number valueNumber;

    private String valueString;

    private boolean valueBool;

    public boolean getValueBool() {
        return valueBool;
    }
}
