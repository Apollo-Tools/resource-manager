package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the all monitored metrics.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum MonitoringMetricEnum {
    /**
     * AVAILABILITY
     */
    AVAILABILITY("availability"),
    /**
     * COST
     */
    COST("cost"),
    /**
     * CPU
     */
    CPU("cpu"),
    /**
     * CPU%
     */
    CPU_UTIL("cpu%"),
    /**
     * LATENCY
     */
    LATENCY("latency"),
    /**
     * MEMORY
     */
    MEMORY("memory"),
    /**
     * MEMORY%
     */
    MEMORY_UTIL("memory%"),
    /**
     * NODE
     */
    NODE("node"),
    /**
     * STORAGE
     */
    STORAGE("storage"),
    /**
     * STORAGE%
     */
    STORAGE_UTIL("storage%"),
    /**
     * UP
     */
    UP("up");

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

    /**
     * Create an instance from a service level objective. This is necessary because a public method
     * is not allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param ensembleSLO the service level objective
     * @return the created object
     */
    public static MonitoringMetricEnum fromEnsembleSLO(EnsembleSLO ensembleSLO) {
        return Arrays.stream(MonitoringMetricEnum.values())
            .filter(value -> value.name.equals(ensembleSLO.getName()))
            .findFirst()
            .orElse(null);
    }
}
