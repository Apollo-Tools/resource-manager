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
@DataObject
public class DeploymentCredentials {

    private String edgeLoginCredentials = "";

    private final List<Credentials> cloudCredentials = new ArrayList<>();

    public DeploymentCredentials(final JsonObject jsonObject) {
        final DeploymentCredentials credentials = jsonObject.mapTo(DeploymentCredentials.class);
        this.edgeLoginCredentials = credentials.getEdgeLoginCredentials();
        this.cloudCredentials.addAll(credentials.getCloudCredentials());
    }


    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
