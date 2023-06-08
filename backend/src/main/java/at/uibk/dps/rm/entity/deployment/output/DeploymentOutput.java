package at.uibk.dps.rm.entity.deployment.output;

import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the terraform output of a deployment.
 *
 * @author matthi-g
 */
@Data
@NoArgsConstructor
public class DeploymentOutput {

    private TFOutput functionUrls;

    /**
     * Create an instance from the terraform output in JSON-format.
     *
     * @param jsonObject the terraform output
     * @return the new object
     */
    public static DeploymentOutput fromJson(JsonObject jsonObject) {
        for (String type : new String[]{"function_urls"}) {
            JsonObject typeUrls = jsonObject.getJsonObject(type);
            typeUrls.remove("sensitive");
            typeUrls.remove("type");
            if (!typeUrls.containsKey("value") || typeUrls.fieldNames().size() > 1) {
                throw new IllegalArgumentException("Schema of json is invalid");
            }
        }
        return jsonObject.mapTo(DeploymentOutput.class);
    }

}
