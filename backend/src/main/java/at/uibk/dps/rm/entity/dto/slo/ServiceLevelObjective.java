package at.uibk.dps.rm.entity.dto.slo;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.util.serialization.ServiceLevelObjectiveDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
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

    /**
     * Create a new instance from a {@link EnsembleSLO}.
     *
     * @param ensembleSLO the ensembleSLO
     */
    public ServiceLevelObjective(EnsembleSLO ensembleSLO) {
        List<SLOValue> value = new ArrayList<>();
        if (ensembleSLO.getValueNumbers() != null) {
            ensembleSLO.getValueNumbers().forEach(entry -> {
                SLOValue sloValue = new SLOValue();
                sloValue.setValueNumber(entry);
                sloValue.setSloValueType(SLOValueType.NUMBER);
                value.add(sloValue);
            });
        } else if (ensembleSLO.getValueBools() != null) {
            ensembleSLO.getValueBools().forEach(entry -> {
                SLOValue sloValue = new SLOValue();
                sloValue.setValueBool(entry);
                sloValue.setSloValueType(SLOValueType.BOOLEAN);
                value.add(sloValue);
            });
        } else if (ensembleSLO.getValueStrings() != null) {
            ensembleSLO.getValueStrings().forEach(entry -> {
                SLOValue sloValue = new SLOValue();
                sloValue.setValueString(entry);
                sloValue.setSloValueType(SLOValueType.STRING);
                value.add(sloValue);
            });
        }
        this.setName(ensembleSLO.getName());
        this.setExpression(ensembleSLO.getExpression());
        this.setValue(value);
    }

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
