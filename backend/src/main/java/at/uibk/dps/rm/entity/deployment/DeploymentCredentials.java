package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.entity.model.Credentials;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that is used in the deployment process to store credentials for cloud and edge resources.
 *
 * @author matthi-g
 */
@Data
@NoArgsConstructor
@DataObject
public class DeploymentCredentials {

    private String edgeLoginCredentials = "";

    private final List<Credentials> cloudCredentials = new ArrayList<>();

    /**
     * Create an instance from a JsonObject. The creation fails, if the schema of the JsonObject
     * is wrong.
     *
     * @param jsonObject the JsonObject to create the instance from
     */
    public DeploymentCredentials(final JsonObject jsonObject) {
        final DeploymentCredentials credentials = jsonObject.mapTo(DeploymentCredentials.class);
        this.edgeLoginCredentials = credentials.getEdgeLoginCredentials();
        this.cloudCredentials.addAll(credentials.getCloudCredentials());
    }

    /**
     * Get the object as a JsonObject.
     *
     * @return the object as JsonObject
     */
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
