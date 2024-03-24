package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DataObject
public class StartupShutdownServicesDTO {
    private Deployment deployment;
    private List<ServiceDeployment> serviceDeployments;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public StartupShutdownServicesDTO(JsonObject jsonObject) {
        StartupShutdownServicesDTO deployServicesDTO = jsonObject.mapTo(StartupShutdownServicesDTO.class);
        this.setDeployment(deployServicesDTO.getDeployment());
        this.setServiceDeployments(deployServicesDTO.getServiceDeployments());
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
