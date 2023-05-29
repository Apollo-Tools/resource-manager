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
        if (!ensembleSLO.getValueNumbers().isEmpty()) {
            values = ensembleSLO.getValueNumbers().stream()
                .map(value -> {
                    SLOValue sloValue = new SLOValue();
                    sloValue.setValueNumber(value);
                    sloValue.setSloValueType(SLOValueType.NUMBER);
                    return sloValue;
                })
                .collect(Collectors.toList());
        } else if (!ensembleSLO.getValueStrings().isEmpty()) {
            values = ensembleSLO.getValueStrings().stream()
                .map(value -> {
                    SLOValue sloValue = new SLOValue();
                    sloValue.setValueString(value);
                    sloValue.setSloValueType(SLOValueType.STRING);
                    return sloValue;
                })
                .collect(Collectors.toList());
        } else {
            values = ensembleSLO.getValueBools().stream()
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
