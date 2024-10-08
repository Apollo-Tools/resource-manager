package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Represents a DTO that is used for the validation of an active deployment.
 *
 * @author matthi-g
 */
@Data
@RequiredArgsConstructor
public class DeploymentAlertingDTO {

    private long deploymentId;

    private long ensembleId;

    private String alertingUrl;

    private List<Resource> resources;

    private List<EnsembleSLO> ensembleSLOs;
}
