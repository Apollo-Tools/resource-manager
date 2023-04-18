package at.uibk.dps.rm.entity.deployment.output;

import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeploymentOutput {

    private TFOutput edgeUrls;

    private TFOutput functionUrls;

    private TFOutput vmUrls;

    public static DeploymentOutput fromJson(final JsonObject jsonObject) {
        for (final String type : new String[]{"edge_urls", "function_urls", "vm_urls"}) {
            final JsonObject typeUrls = jsonObject.getJsonObject(type);
            typeUrls.remove("sensitive");
            typeUrls.remove("type");
            if (!typeUrls.containsKey("value") || typeUrls.fieldNames().size() > 1) {
                throw new IllegalArgumentException("Schema of json is invalid");
            }
        }
        return jsonObject.mapTo(DeploymentOutput.class);
    }

}
