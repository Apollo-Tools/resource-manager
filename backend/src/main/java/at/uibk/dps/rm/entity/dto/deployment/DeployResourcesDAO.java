package at.uibk.dps.rm.entity.dto.deployment;

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
public class DeployResourcesDAO extends DeployTerminateDAO {

    private DockerCredentials dockerCredentials;

    private String kubeConfig;

    private List<VPC> vpcList;

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public DeployResourcesDAO(JsonObject jsonObject) {
        super();
        DeployResourcesDAO request = jsonObject.mapTo(DeployResourcesDAO.class);
        this.setFunctionDeployments(request.getFunctionDeployments());
        this.setServiceDeployments(request.getServiceDeployments());
        this.setCredentialsList(request.getCredentialsList());
        this.setKubeConfig(request.getKubeConfig());
        this.setDockerCredentials(request.getDockerCredentials());
        this.setDeployment(request.getDeployment());
        this.setVpcList(request.getVpcList());
    }
}
