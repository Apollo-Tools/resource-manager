package at.uibk.dps.rm.entity.dto.slo;

import at.uibk.dps.rm.util.SLOValueSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * Represents a Service Level Objective value of the listFunctionResourcesBySLOs operation.
 *
 * @author matthi-g
 */
@Data
@JsonSerialize(using = SLOValueSerializer.class)
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
