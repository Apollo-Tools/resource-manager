package at.uibk.dps.rm.entity.deployment.output;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Represents one entry from a {@link TFOutputService} object.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class TFOutputValueService {

    private List<String> pods;

    private Map<String, JsonObject> service;
}
