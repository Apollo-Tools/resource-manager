package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DeploymentWithResourcesDTO extends Deployment {

    private List<FunctionDeployment> functionResources = new ArrayList<>();

    private List<ServiceDeployment> serviceResources = new ArrayList<>();

    private Timestamp createdAt;
}
