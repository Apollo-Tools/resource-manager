package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class DeploymentAlertingDTO {

    private long deploymentId;

    private long ensembleId;

    private String alertingUrl;

    private List<Resource> resources;

    private List<EnsembleSLO> ensembleSLOs;
}
