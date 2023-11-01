package at.uibk.dps.rm.entity.dto.function;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvocationResponseDTO {

    private InvocationMonitoringDTO monitoringData;

    private String body;
}
