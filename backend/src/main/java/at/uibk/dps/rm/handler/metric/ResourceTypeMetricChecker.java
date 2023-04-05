package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.ResourceTypeMetricService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ResourceTypeMetricChecker extends EntityChecker {

    private final ResourceTypeMetricService service;

    public ResourceTypeMetricChecker(ResourceTypeMetricService service) {
        super(service);
        this.service = service;
    }

    public Completable checkMissingRequiredResourceTypeMetricsByResource(long resourceId) {
        Single<Boolean> checkMissingMetrics = service.missingRequiredResourceTypeMetricsByResourceId(resourceId);
        return ErrorHandler.handleMissingRequiredMetrics(checkMissingMetrics).ignoreElement();
    }

    public Completable checkMissingRequiredMetricsByFunctionResources(List<JsonObject> functionResources) {
        List<Completable> completables = new ArrayList<>();
        HashSet<Long> resourceIds = new HashSet<>();
        for (JsonObject functionResource : functionResources) {
            Resource resource = functionResource.mapTo(FunctionResource.class).getResource();
            if (!resourceIds.contains(resource.getResourceId())) {
                completables.add(this.checkMissingRequiredResourceTypeMetricsByResource(resource.getResourceId()));
                resourceIds.add(resource.getResourceId());
            }
        }
        return Completable.merge(completables);
    }
}
