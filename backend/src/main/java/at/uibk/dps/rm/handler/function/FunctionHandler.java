package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.ext.web.FileUpload;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the function entity.
 *
 * @author matthi-g
 */
public class FunctionHandler extends ValidationHandler {

    private final RuntimeChecker runtimeChecker;

    /**
     * Create an instance from the functionChecker and runtimeChecker.
     *
     * @param functionChecker the function checker
     * @param runtimeChecker the runtime checker
     */
    public FunctionHandler(FunctionChecker functionChecker, RuntimeChecker runtimeChecker) {
        super(functionChecker);
        this.runtimeChecker = runtimeChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = new JsonObject();
        if (rc.request().headers().get("Content-Type").equals("application/json")) {
            requestBody = rc.body().asJsonObject();
            requestBody.put("is_file", false);
        } else {
            MultiMap attributes = rc.request().formAttributes();
            requestBody.put("name", attributes.get("name"));
            requestBody.put("runtime", new JsonObject(attributes.get("runtime")));
            FileUpload file = rc.fileUploads().get(0);
            requestBody.put("code", file.uploadedFileName());
            requestBody.put("is_file", true);
        }
        JsonObject finalRequestBody = requestBody;
        return runtimeChecker.checkExistsOne(requestBody
                .getJsonObject("runtime")
                .getLong("runtime_id"))
            .andThen(entityChecker.checkForDuplicateEntity(requestBody))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(finalRequestBody));
    }
}
