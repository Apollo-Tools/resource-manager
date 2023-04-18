package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
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
public class DeployResourcesRequest extends DeployTerminateRequest {

    private DockerCredentials dockerCredentials;

    private List<VPC> vpcList;

    public DeployResourcesRequest(final JsonObject jsonObject) {
        super();
        DeployResourcesRequest request = jsonObject.mapTo(DeployResourcesRequest.class);
        this.setFunctionResources(request.getFunctionResources());
        this.setCredentialsList(request.getCredentialsList());
        this.setDockerCredentials(request.getDockerCredentials());
        this.setReservation(request.getReservation());
        this.setVpcList(request.getVpcList());
    }
}
