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
     * Environment
     */
    ENVIRONMENT("environment"),
    /**
     * Resource type
     */
    RESOURCE_TYPE("resource_type"),
    /**
     * Platform
     */
    PLATFORM("platform"),
    /**
     * Region
     */
    REGION("region"),
    /**
     * Resource provider
     */
    RESOURCE_PROVIDER("resource_provider"),
    /**
     * Metric
     */
    METRIC("metric");

    private final String value;
}
