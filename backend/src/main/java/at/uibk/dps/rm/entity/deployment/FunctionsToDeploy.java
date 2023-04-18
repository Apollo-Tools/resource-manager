package at.uibk.dps.rm.entity.deployment;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@DataObject(generateConverter = true, publicConverter = false)
public class FunctionsToDeploy {

    private String functionsString = "new StringBuilder()";

    private final List<String> functionIdentifiers = new ArrayList<>();

    public FunctionsToDeploy(final JsonObject jsonObject) {
        final FunctionsToDeploy functionsToDeploy = jsonObject.mapTo(FunctionsToDeploy.class);
        this.functionsString = functionsToDeploy.getFunctionsString();
        this.functionIdentifiers.addAll(functionsToDeploy.getFunctionIdentifiers());
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
