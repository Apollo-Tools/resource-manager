package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionResourceHandler extends ValidationHandler {

    private final FunctionResourceChecker functionResourceChecker;

    private final FunctionChecker functionChecker;

    private final ResourceChecker resourceChecker;

    public FunctionResourceHandler(FunctionResourceService functionResourceService, FunctionService functionService,
                                   ResourceService resourceService) {
        super(new FunctionResourceChecker(functionResourceService));
        functionResourceChecker = (FunctionResourceChecker) super.entityChecker;
        functionChecker = new FunctionChecker(functionService);
        resourceChecker = new ResourceChecker(resourceService);
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
                .flatMap(ids -> checkDeleteFunctionResourceExists(ids.get("functionId"), ids.get("resourceId"))
                    .andThen(Single.just(ids))
                ))
            .flatMapCompletable(ids -> functionResourceChecker.submitDeleteFunctionResource(
                ids.get("functionId"), ids.get("resourceId")));
    }


    protected Single<List<FunctionResource>> checkAddResourcesExist(JsonArray requestBody, long functionId) {
        List<FunctionResource> functionResources = new ArrayList<>();
        List<Completable> completables = checkAddResourcesList(requestBody, functionId, functionResources);
        return Completable.merge(completables)
            .andThen(Single.just(functionResources));
    }

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

    protected Completable checkDeleteFunctionResourceExists(long functionId, long resourceId) {
        return Completable.mergeArray(
            functionChecker.checkExistsOne(functionId),
            resourceChecker.checkExistsOne(resourceId),
            functionResourceChecker.checkExistsByFunctionAndResource(functionId, resourceId));
    }
}