package at.uibk.dps.rm.entity.dto.function;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvocationMonitoringDTO {

    private double executionTimeMs;

    private long startTimestamp;
}
