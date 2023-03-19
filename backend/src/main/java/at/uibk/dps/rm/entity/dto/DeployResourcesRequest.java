package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.VPC;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class DeployResourcesRequest {

    public DeployResourcesRequest(JsonObject jsonObject) {
        DeployResourcesRequest request = jsonObject.mapTo(DeployResourcesRequest.class);
        this.setFunctionResources(request.getFunctionResources());
        this.setCredentialsList(request.getCredentialsList());
        this.setDockerCredentials(request.getDockerCredentials());
        this.setReservation(request.getReservation());
        this.setVpcList(request.getVpcList());
    }

    private Reservation reservation;

    private List<Credentials> credentialsList;

    private List<FunctionResource> functionResources;

    private DockerCredentials dockerCredentials;

    private List<VPC> vpcList;

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
