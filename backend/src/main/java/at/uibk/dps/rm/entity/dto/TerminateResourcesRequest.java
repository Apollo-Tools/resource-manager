package at.uibk.dps.rm.entity.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@DataObject
public class TerminateResourcesRequest extends DeployTerminateRequest {
    public TerminateResourcesRequest(final JsonObject jsonObject) {
        super();
        final TerminateResourcesRequest request = jsonObject.mapTo(TerminateResourcesRequest.class);
        this.setFunctionResources(request.getFunctionResources());
        this.setCredentialsList(request.getCredentialsList());
        this.setReservation(request.getReservation());
    }
}
