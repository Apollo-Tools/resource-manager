package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an existing deployment, that contains resources.
 *
 * @author matthi-g
 */
@Getter
@Setter
@JsonIgnoreProperties(value = {"function_deployments", "service_deployments", "service_state_change_in_progress",
    "locked_resources"})
public class DeploymentWithResourcesDTO extends Deployment {

    private List<FunctionDeployment> functionResources = new ArrayList<>();

    private List<ServiceDeployment> serviceResources = new ArrayList<>();

    private Timestamp createdAt;

    private Timestamp finishedAt;
}
