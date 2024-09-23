package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.List;
import java.util.Objects;

/**
 * Represents the output of the startup/shutdown services operation.
 * Contains the deployment and the service deployments.
 *
 * @author matthi-g
 */
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

    @Generated
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        StartupShutdownServicesDTO that = (StartupShutdownServicesDTO) obj;

        if (!Objects.equals(deployment, that.deployment)) return false;
        return Objects.equals(serviceDeployments, that.serviceDeployments);
    }

    @Generated
    @Override
    public int hashCode() {
        int result = deployment != null ? deployment.hashCode() : 0;
        result = 31 * result + (serviceDeployments != null ? serviceDeployments.hashCode() : 0);
        return result;
    }
}
