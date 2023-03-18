package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.entity.model.Credentials;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@DataObject(generateConverter = true, publicConverter = false)
public class DeploymentCredentials {

    public DeploymentCredentials(JsonObject jsonObject) {
        DeploymentCredentials credentials = jsonObject.mapTo(DeploymentCredentials.class);
        this.edgeLoginCredentials.append(credentials.getEdgeLoginCredentials());
        this.cloudCredentials.addAll(credentials.getCloudCredentials());
    }

    private final StringBuilder edgeLoginCredentials = new StringBuilder();

    private final List<Credentials> cloudCredentials = new ArrayList<>();


    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
