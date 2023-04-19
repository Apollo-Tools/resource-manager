package at.uibk.dps.rm.entity.dto.slo;

import lombok.Data;

/**
 * Represents a Service Level Objective value of the listFunctionResourcesBySLOs operation.
 *
 * @author matthi-g
 */
@Data
public class SLOValue {
    private SLOValueType sloValueType;

    private Number valueNumber;

    private String valueString;

    private boolean valueBool;

    @SuppressWarnings("PMD")
    public boolean getValueBool() {
        return valueBool;
    }
}
