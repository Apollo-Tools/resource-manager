package at.uibk.dps.rm.entity.dto.deployment;

import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A DTO to simplify transport of the relevant deployment and termination data on the even bus.
 *
 * @author matthi-g
 */
@Getter
@Setter
public abstract class DeployTerminateDTO {

    private Deployment deployment;

    private List<Credentials> credentialsList;

    private List<FunctionDeployment> functionDeployments;

    private List<ServiceDeployment> serviceDeployments;

    /**
     * Get the object as a JsonObject.
     *
     * @return the object as JsonObject
     */
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
