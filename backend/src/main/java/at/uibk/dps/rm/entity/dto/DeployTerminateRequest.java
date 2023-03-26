package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Reservation;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class DeployTerminateRequest {

    private Reservation reservation;

    private List<Credentials> credentialsList;

    private List<FunctionResource> functionResources;

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
