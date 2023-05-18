package at.uibk.dps.rm.entity.dto.slo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the supported service level objectives.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum SLOType {
    /**
     * Region
     */
    REGION("region"),
    /**
     * Resource provider
     */
    RESOURCE_PROVIDER("resource_provider"),
    /**
     * Resource type
     */
    RESOURCE_TYPE("resource_type"),
    /**
     * Metric
     */
    METRIC("metric");

    private final String value;
}
