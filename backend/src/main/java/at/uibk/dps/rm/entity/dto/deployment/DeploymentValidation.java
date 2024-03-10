package at.uibk.dps.rm.entity.dto.deployment;

import lombok.Data;

/**
 * Represents the validation section of the DeployResourcesRequest.
 *
 * @author matthi-g
 */
@Data
public class DeploymentValidation {

    private long ensembleId;

    private String alertNotificationUrl;
}
