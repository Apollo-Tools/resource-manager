package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.util.toscamapping.TOSCAFile;
import at.uibk.dps.rm.util.toscamapping.TOSCAMapper;
import at.uibk.dps.rm.util.validation.EntityNameValidator;
import at.uibk.dps.rm.util.validation.JsonArrayValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

/**
 * Used to validate the inputs of the resource endpoint and fails the
 * context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class ResourceInputHandler {

    /**
     * Validate a add resources request.
     *
     * @param rc the routing context
     */
    public static void validateAddResourceRequest(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        String resourceName = requestBody.getString("name");
        EntityNameValidator.checkName(resourceName, Resource.class)
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    public static void validateAddStandardizedResourceRequest(RoutingContext rc) {
        String requestBody = rc.body().asString();
        System.out.println(requestBody);
        TOSCAFile toscaFile = null;

        try {
            toscaFile = TOSCAMapper.readTOSCA(requestBody);
            System.out.println(toscaFile.getTopology_template().getNode_templates().get("resource_1").getCapabilities().get("resource").getProperties().get("name"));
        } catch (JsonProcessingException e) {
             rc.fail(400,e);
        }
        EntityNameValidator.checkName(toscaFile.getTopology_template().getNode_templates().get("resource_1").getCapabilities().get("resource").getProperties().get("name").toString(), Resource.class)
                .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    /**
     * Validate if a add metrics request contains duplicated metrics.
     *
     * @param rc the routing context
     */
    public static void validateAddMetricsRequest(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        JsonArrayValidator.checkJsonArrayDuplicates(requestBody, "metric_id")
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }
}
