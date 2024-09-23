package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the output of the setup terraform modules operation.
 * Contains the terraform modules and the deployment credentials.
 *
 * @author matthi-g
 */
@Data
@NoArgsConstructor
@DataObject
public class SetupTFModulesOutputDTO {

    private List<TerraformModule> terraformModules;

    private DeploymentCredentials deploymentCredentials;

    /**
     * Create an instance from a JsonObject.
     *
     * @param jsonObject the JsonObject to create the instance from
     */
    public SetupTFModulesOutputDTO(JsonObject jsonObject) {
        SetupTFModulesOutputDTO outputDTO = jsonObject.mapTo(SetupTFModulesOutputDTO.class);
        this.terraformModules = outputDTO.getTerraformModules();
        this.deploymentCredentials = outputDTO.getDeploymentCredentials();
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
