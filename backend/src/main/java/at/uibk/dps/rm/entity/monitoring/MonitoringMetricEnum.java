package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum MonitoringMetricEnum {
    /**
     * CPU%
     */
    CPU_UTIL("cpu%");

    private final String name;

    /**
     * Create an instance from a service level objective. This is necessary because a public method
     * is not allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param serviceLevelObjective the service level objective
     * @return the created object
     */
    public static MonitoringMetricEnum fromSLO(ServiceLevelObjective serviceLevelObjective) {
        return Arrays.stream(MonitoringMetricEnum.values())
            .filter(value -> value.name.equals(serviceLevelObjective.getName()))
            .findFirst()
            .orElse(null);
    }
}
