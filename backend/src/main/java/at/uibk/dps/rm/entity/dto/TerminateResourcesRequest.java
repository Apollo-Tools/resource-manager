package at.uibk.dps.rm.entity.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

/**
 * A DAO to simplify transport of the relevant termination data on the even bus.
 *
 * @author matthi-g
 */
@NoArgsConstructor
@DataObject
public class TerminateResourcesRequest extends DeployTerminateRequest {

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public TerminateResourcesRequest(JsonObject jsonObject) {
        super();
        TerminateResourcesRequest request = jsonObject.mapTo(TerminateResourcesRequest.class);
        this.setFunctionReservations(request.getFunctionReservations());
        this.setServiceReservations(request.getServiceReservations());
        this.setCredentialsList(request.getCredentialsList());
        this.setReservation(request.getReservation());
    }
}
