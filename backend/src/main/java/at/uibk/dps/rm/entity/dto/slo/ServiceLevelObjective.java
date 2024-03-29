package at.uibk.dps.rm.entity.dto.slo;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.util.serialization.ServiceLevelObjectiveDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Represents a Service Level Objective of the listFunctionResourcesBySLOs operation.
 *
 * @author matthi-g
 */
@Data
@AllArgsConstructor
@JsonDeserialize(using = ServiceLevelObjectiveDeserializer.class)
public class ServiceLevelObjective {
    private String name;

    private ExpressionType expression;

    private List<SLOValue> value;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ServiceLevelObjective that = (ServiceLevelObjective) obj;
        return name.equals(that.name);
    }

    @Override
    @Generated
    public int hashCode() {
        return name.hashCode();
    }
}
