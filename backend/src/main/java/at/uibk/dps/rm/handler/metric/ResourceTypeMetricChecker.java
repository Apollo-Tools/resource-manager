package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.ResourceTypeMetricService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class ResourceTypeMetricChecker extends EntityChecker {

    private final ResourceTypeMetricService service;

    public ResourceTypeMetricChecker(ResourceTypeMetricService service) {
        super(service);
        this.service = service;
    }

    public Completable checkMissingRequiredResourceTypeMetrics(long resourceId) {
        Single<Boolean> checkMissingMetrics = service.missingRequiredResourceTypeMetricsByResourceId(resourceId);
        return ErrorHandler.handleMissingRequiredMetrics(checkMissingMetrics).ignoreElement();
    }
}
