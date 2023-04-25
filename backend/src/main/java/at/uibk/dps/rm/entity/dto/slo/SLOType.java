package at.uibk.dps.rm.entity.dto.slo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SLOType {
    REGION("region"),
    RESOURCE_PROVIDER("resource_provider"),
    RESOURCE_TYPE("resource_type"),
    METRIC("metric");

    private final String value;
}
