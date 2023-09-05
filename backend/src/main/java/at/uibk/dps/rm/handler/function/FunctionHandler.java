package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
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

    private final FunctionService functionService;

    /**
     * Create an instance from the functionService.
     *
     * @param functionService the service
     */
    public FunctionHandler(FunctionService functionService) {
        super(functionService);
        this.functionService = functionService;
    }

    @Override
    public Single<JsonObject> postOneToAccount(RoutingContext rc) {
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
            requestBody.put("is_public", Boolean.valueOf(attributes.get("is_public")));
            FileUpload file = rc.fileUploads().get(0);
            String[] filePath = file.uploadedFileName()
                .replace("\\", "/")
                .split("/");
            requestBody.put("code", filePath[filePath.length - 1]);
        }
        requestBody.put("is_file", isFile);
        long accountId = rc.user().principal().getLong("account_id");
        return functionService.saveToAccount(accountId, requestBody);
    }

    @Override
    protected Completable updateOneOwned(RoutingContext rc) {
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
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> functionService.updateOwned(id, accountId, requestBody));
    }

    @Override
    public Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return functionService.findAllAccessibleFunctions(accountId);
    }

    @Override
    public Single<JsonArray> getAllFromAccount(RoutingContext rc) {
        return super.getAllFromAccount(rc);
    }
}
