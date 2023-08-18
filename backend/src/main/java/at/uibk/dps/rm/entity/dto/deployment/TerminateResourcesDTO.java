package at.uibk.dps.rm.entity.dto.deployment;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

/**
 * A DTO to simplify transport of the relevant termination data on the even bus.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@DataObject
public class TerminateResourcesDTO extends DeployTerminateDTO {

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public TerminateResourcesDTO(JsonObject jsonObject) {
        super();
        TerminateResourcesDTO request = jsonObject.mapTo(TerminateResourcesDTO.class);
        this.setFunctionDeployments(request.getFunctionDeployments());
        this.setServiceDeployments(request.getServiceDeployments());
        this.setCredentialsList(request.getCredentialsList());
        this.setDeployment(request.getDeployment());
    }

    /**
     * Transform a {@link DeployResourcesDTO} into a {@link TerminateResourcesDTO}.
     *
     * @param deployResourcesDTO the deploy resources object
     */
    public TerminateResourcesDTO(DeployResourcesDTO deployResourcesDTO) {
        this.setDeployment(deployResourcesDTO.getDeployment());
        this.setFunctionDeployments(deployResourcesDTO.getFunctionDeployments());
        this.setServiceDeployments(deployResourcesDTO.getServiceDeployments());
        this.setCredentialsList(deployResourcesDTO.getCredentialsList());
    }
}
