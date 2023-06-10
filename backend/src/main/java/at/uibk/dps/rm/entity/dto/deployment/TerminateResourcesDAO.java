package at.uibk.dps.rm.entity.dto.deployment;

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
public class TerminateResourcesDAO extends DeployTerminateDAO {

    /**
     * Create an instance with a JsonObject.
     *
     * @param jsonObject the JsonObject
     */
    public TerminateResourcesDAO(JsonObject jsonObject) {
        super();
        TerminateResourcesDAO request = jsonObject.mapTo(TerminateResourcesDAO.class);
        this.setFunctionDeployments(request.getFunctionDeployments());
        this.setServiceDeployments(request.getServiceDeployments());
        this.setCredentialsList(request.getCredentialsList());
        this.setDeployment(request.getDeployment());
    }
}
