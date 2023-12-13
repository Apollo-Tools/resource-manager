package at.uibk.dps.rm.entity.dto.function;

import lombok.Getter;
import lombok.Setter;

/**
 * A DTO that represents the monitoring data of a function invocation that is part of the
 * {@link InvocationResponseBodyDTO}.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class InvocationMonitoringDTO {

    private double executionTimeMs;

    private long startTimestamp;
}
