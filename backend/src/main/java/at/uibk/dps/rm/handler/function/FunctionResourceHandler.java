package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processes the http requests that concern the function_resource entity.
 *
 * @author matthi-g
 */
public class FunctionResourceHandler extends ValidationHandler {

    private final FunctionResourceChecker functionResourceChecker;

    private final FunctionChecker functionChecker;

    private final ResourceChecker resourceChecker;

    /**
     * Create an instance from the functionResourceChecker, functionChecker and resourceChecker.
     *
     * @param functionResourceChecker the function resource checker
     * @param functionChecker the function checker
     * @param resourceChecker the resource checker
     */
    public FunctionResourceHandler(FunctionResourceChecker functionResourceChecker, FunctionChecker functionChecker,
                                   ResourceChecker resourceChecker) {
        super(functionResourceChecker);
        this.functionResourceChecker = functionResourceChecker;
        this.functionChecker = functionChecker;
        this.resourceChecker = resourceChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> functionChecker.checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMap(resourceChecker::checkFindAllByFunction);
    }


    @Override
    public Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> functionChecker.checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMapCompletable(id -> checkAddResourcesExist(requestBody, id)
                .flatMapCompletable(functionResources -> {
                    JsonArray functionResourcesJson = Json.encodeToBuffer(functionResources)
                        .toJsonArray();
                    functionResourcesJson.forEach(entry -> {
                        JsonObject functionResource = (JsonObject) entry;
                        JsonObject function = new JsonObject();
                        function.put("function_id", id);
                        functionResource.put("function", function);
                    });
                    return functionResourceChecker.submitCreateAll(functionResourcesJson);
                }));
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "functionId")
            .flatMap(functionId -> HttpHelper.getLongPathParam(rc, "resourceId")
                .map(resourceId -> Map.of("functionId", functionId, "resourceId", resourceId))
                .flatMap(ids -> checkFunctionResourceExists(ids.get("functionId"), ids.get("resourceId"))
                    .andThen(Single.just(ids))
                ))
            .flatMapCompletable(ids -> functionResourceChecker.submitDeleteFunctionResource(
                ids.get("functionId"), ids.get("resourceId")));
    }


    /**
     * Check if the resources that should be added to the function are already present and exist.
     *
     * @param requestBody the request body that contains the resources
     * @param functionId the id of the function
     * @return a Single that emits a list of function resources
     */
    protected Single<List<FunctionResource>> checkAddResourcesExist(JsonArray requestBody, long functionId) {
        List<FunctionResource> functionResources = new ArrayList<>();
        List<Completable> completables = checkAddResourcesList(requestBody, functionId, functionResources);
        return Completable.merge(completables)
            .andThen(Single.just(functionResources));
    }

    /**
     * Check if adding resources to a function would cause any violations.
     *
     * @param requestBody the request body that contains the resources to add
     * @param functionId the id of the function
     * @param functionResources the list where the new function resources are stored.
     * @return a List of completables
     */
    private List<Completable> checkAddResourcesList(JsonArray requestBody, long functionId,
                                                    List<FunctionResource> functionResources) {
        Function function = new Function();
        function.setFunctionId(functionId);
        List<Completable> completables = new ArrayList<>();
        requestBody.stream().forEach(jsonObject -> {
            JsonObject jsonResource = (JsonObject) jsonObject;
            long resourceId = jsonResource.getLong("resource_id");
            Resource resource = new Resource();
            resource.setResourceId(resourceId);
            FunctionResource functionResource = new FunctionResource();
            functionResource.setResource(resource);
            functionResource.setFunction(function);
            functionResources.add(functionResource);
            completables.add(resourceChecker.checkExistsOne(resourceId));
            completables.add(functionResourceChecker.checkForDuplicateByFunctionAndResource(functionId, resourceId));
        });
        return completables;
    }

    /**
     * Check if a function resource exists by its functionId and resourceId.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a Completable
     */
    protected Completable checkFunctionResourceExists(long functionId, long resourceId) {
        return Completable.mergeArray(
            functionChecker.checkExistsOne(functionId),
            resourceChecker.checkExistsOne(resourceId),
            functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId));
    }
}
