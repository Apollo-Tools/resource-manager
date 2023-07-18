package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.dto.credentials.DeploymentCredentials;
import at.uibk.dps.rm.entity.model.VPC;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.List;

/**
 * A DTO to simplify transport of the relevant deployment data on the event bus.
 *
 * @author matthi-g
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class DeployResourcesDTO extends DeployTerminateDTO {

    private DeploymentCredentials deploymentCredentials;

    private List<VPC> vpcList;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public DeployResourcesDTO(JsonObject jsonObject) {
        super();
        DeployResourcesDTO request = jsonObject.mapTo(DeployResourcesDTO.class);
        this.setFunctionDeployments(request.getFunctionDeployments());
        this.setServiceDeployments(request.getServiceDeployments());
        this.setCredentialsList(request.getCredentialsList());
        this.setDeploymentCredentials(request.getDeploymentCredentials());
        this.setDeployment(request.getDeployment());
        this.setVpcList(request.getVpcList());
    }
}
