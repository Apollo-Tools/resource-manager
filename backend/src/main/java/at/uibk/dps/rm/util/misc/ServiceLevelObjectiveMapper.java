package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class to map entities to {@link ServiceLevelObjective}s.
 *
 * @author matthi-g
 */
@UtilityClass
public class ServiceLevelObjectiveMapper {

    /**
     * Map an ensembleSLO to a ServiceLevelObjective.
     *
     * @param ensembleSLO the ensemble slo
     * @return the mapped service level objective
     */
    public static ServiceLevelObjective mapEnsembleSLO(EnsembleSLO ensembleSLO) {
        List<SLOValue> values;
        List<Double> valueNumbers = ensembleSLO.getValueNumbers();
        List<String> valueStrings = ensembleSLO.getValueStrings();
        List<Boolean> valueBools = ensembleSLO.getValueBools();
        if (valueNumbers != null && !valueNumbers.isEmpty()) {
            values = valueNumbers.stream()
                .map(value -> {
                    SLOValue sloValue = new SLOValue();
                    sloValue.setValueNumber(value);
                    sloValue.setSloValueType(SLOValueType.NUMBER);
                    return sloValue;
                })
                .collect(Collectors.toList());
        } else if (valueStrings != null && !valueStrings.isEmpty()) {
            values = valueStrings.stream()
                .map(value -> {
                    SLOValue sloValue = new SLOValue();
                    sloValue.setValueString(value);
                    sloValue.setSloValueType(SLOValueType.STRING);
                    return sloValue;
                })
                .collect(Collectors.toList());
        } else {
            values = valueBools.stream()
                .map(value -> {
                    SLOValue sloValue = new SLOValue();
                    sloValue.setValueBool(value);
                    sloValue.setSloValueType(SLOValueType.BOOLEAN);
                    return sloValue;
                })
                .collect(Collectors.toList());
        }
        return new ServiceLevelObjective(ensembleSLO.getName(), ensembleSLO.getExpression(), values);
    }
}
