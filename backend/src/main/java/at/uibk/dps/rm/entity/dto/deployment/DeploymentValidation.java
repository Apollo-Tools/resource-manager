package at.uibk.dps.rm.entity.dto.deployment;

import lombok.Data;

@Data
public class DeploymentValidation {

    private long ensembleId;

    private String alertNotificationUrl;
}
