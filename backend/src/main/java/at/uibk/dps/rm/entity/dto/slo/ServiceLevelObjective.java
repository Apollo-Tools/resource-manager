package at.uibk.dps.rm.entity.dto.slo;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.util.ServiceLevelObjectiveDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonDeserialize(using = ServiceLevelObjectiveDeserializer.class)
public class ServiceLevelObjective {
    private String name;

    private ExpressionType expression;

    private List<SLOValue> value;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceLevelObjective that = (ServiceLevelObjective) o;

        return name.equals(that.name);
    }

    @Override
    @Generated
    public int hashCode() {
        return name.hashCode();
    }
}
