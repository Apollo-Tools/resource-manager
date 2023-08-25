package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
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

    /**
     * Create an instance from the functionChecker.
     *
     * @param functionChecker the function checker
     */
    public FunctionHandler(FunctionChecker functionChecker) {
        super(functionChecker);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody;
        boolean isFile;
        if (rc.request().headers().get("Content-Type").equals("application/json")) {
            isFile = false;
            requestBody = rc.body().asJsonObject();
        } else {
            isFile = true;
            MultiMap attributes = rc.request().formAttributes();
            requestBody = new JsonObject();
            requestBody.put("name", attributes.get("name"));
            requestBody.put("runtime", new JsonObject(attributes.get("runtime")));
            requestBody.put("function_type", new JsonObject(attributes.get("function_type")));
            requestBody.put("timeout_seconds", Short.valueOf(attributes.get("timeout_seconds")));
            requestBody.put("memory_megabytes", Short.valueOf(attributes.get("memory_megabytes")));
            FileUpload file = rc.fileUploads().get(0);
            String[] filePath = file.uploadedFileName()
                .replace("\\", "/")
                .split("/");
            requestBody.put("code", filePath[filePath.length - 1]);
        }
        requestBody.put("is_file", isFile);
        return entityChecker.submitCreate(requestBody);
    }

    @Override
    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody;
        boolean isFile;
        if (rc.request().headers().get("Content-Type").equals("application/json")) {
            isFile = false;
            requestBody = rc.body().asJsonObject();
        } else {
            isFile = true;
            requestBody = new JsonObject();
            FileUpload file = rc.fileUploads().get(0);
            String[] filePath = file.uploadedFileName()
                .replace("\\", "/")
                .split("/");
            requestBody.put("code", filePath[filePath.length - 1]);
        }
        requestBody.put("is_file", isFile);
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> entityChecker.submitUpdate(id, requestBody));
    }

    @Override
    public Single<JsonArray> getAll(RoutingContext rc) {
        return super.getAll(rc);
    }

    @Override
    public Single<JsonArray> getAllFromAccount(RoutingContext rc) {
        return super.getAllFromAccount(rc);
    }
}
