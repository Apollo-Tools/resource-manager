package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.VPC;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.List;

/**
 * A DAO to simplify transport of the relevant deployment data on the even bus.
 *
 * @author matthi-g
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class DeployResourcesRequest extends DeployTerminateRequest {

    private DockerCredentials dockerCredentials;

    private String kubeConfig;

    private List<VPC> vpcList;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public DeployResourcesRequest(JsonObject jsonObject) {
        super();
        DeployResourcesRequest request = jsonObject.mapTo(DeployResourcesRequest.class);
        this.setFunctionReservations(request.getFunctionReservations());
        this.setServiceReservations(request.getServiceReservations());
        this.setCredentialsList(request.getCredentialsList());
        this.setKubeConfig(request.getKubeConfig());
        this.setDockerCredentials(request.getDockerCredentials());
        this.setReservation(request.getReservation());
        this.setVpcList(request.getVpcList());
    }
}
