package at.uibk.dps.rm.entity.dto.function;

import lombok.Getter;
import lombok.Setter;

/**
 * A DTO that represents the monitoring data and response data of a function invocation.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class InvocationResponseBodyDTO {

    private InvocationMonitoringDTO monitoringData;

    private String body;
}
