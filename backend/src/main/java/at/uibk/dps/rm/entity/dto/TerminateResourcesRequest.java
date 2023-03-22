package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Reservation;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@DataObject
@NoArgsConstructor
public class TerminateResourcesRequest {

    public TerminateResourcesRequest(JsonObject jsonObject) {
        TerminateResourcesRequest request = jsonObject.mapTo(TerminateResourcesRequest.class);
        this.setFunctionResources(request.getFunctionResources());
        this.setCredentialsList(request.getCredentialsList());
        this.setReservation(request.getReservation());
    }

    private Reservation reservation;

    private List<Credentials> credentialsList;

    private List<FunctionResource> functionResources;

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
