package at.uibk.dps.rm.entity.deployment;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Class that is used in the deployment process to store the identifiers and names of functions,
 * that have to be
 * deployed.
 *
 * @author matthi-g
 */
@Data
@NoArgsConstructor
@DataObject(generateConverter = true, publicConverter = false)
public class FunctionsToDeploy {

    private String dockerFunctionsString = "";

    private final List<String> dockerFunctionIdentifiers = new ArrayList<>();

    private final Map<String, Set<String>> functionArchitectures = new HashMap<>();

    /**
     * Create an instance from a JsonObject. The creation fails, if the schema of the JsonObject
     * is wrong.
     *
     * @param jsonObject the JsonObject to create the instance from
     */
    public FunctionsToDeploy(JsonObject jsonObject) {
        FunctionsToDeploy functionsToDeploy = jsonObject.mapTo(FunctionsToDeploy.class);
        this.dockerFunctionsString = functionsToDeploy.getDockerFunctionsString();
        this.dockerFunctionIdentifiers.addAll(functionsToDeploy.getDockerFunctionIdentifiers());
        this.functionArchitectures.putAll(functionsToDeploy.getFunctionArchitectures());
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
