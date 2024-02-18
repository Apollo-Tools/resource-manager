package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.credentials.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentValidation;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the body of the deployResources operation.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@Data
@DataObject
public class DeployResourcesRequest {
    private List<FunctionResourceIds> functionResources;

    private List<ServiceResourceIds> serviceResources;

    private List<ResourceId> lockResources;

    private DeploymentCredentials credentials = new DeploymentCredentials();

    private DeploymentValidation validation;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public DeployResourcesRequest(JsonObject jsonObject) {
        DeployResourcesRequest request = jsonObject.mapTo(DeployResourcesRequest.class);
        this.setFunctionResources(request.getFunctionResources());
        this.setServiceResources(request.getServiceResources());
        this.setLockResources(request.getLockResources());
        this.setCredentials(request.getCredentials());
    }

    /**
     * Get the object as a JsonObject.
     *
     * @return the object as JsonObject
     */
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
