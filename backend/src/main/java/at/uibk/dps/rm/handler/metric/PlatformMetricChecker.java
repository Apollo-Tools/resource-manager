package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.PlatformMetricService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Implements methods to perform CRUD operations on the platform_metric entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class PlatformMetricChecker extends EntityChecker {

    private final PlatformMetricService service;

    /**
     * Create an instance from the platformMetricService.
     *
     * @param service the platform metric service
     */
    public PlatformMetricChecker(PlatformMetricService service) {
        super(service);
        this.service = service;
    }

    /**
     * Check if a resource does have all required metric values registered.
     *
     * @param resourceId the id of the resource
     * @return a Completable
     */
    public Completable checkMissingRequiredPlatformMetricsByResource(long resourceId) {
        Single<Boolean> checkMissingMetrics = service.missingRequiredPlatformMetricsByResourceId(resourceId);
        return ErrorHandler.handleMissingRequiredMetrics(checkMissingMetrics).ignoreElement();
    }

    /**
     * Check for all resources if they do have all required metric values stored.
     *
     * @param resources the resources
     * @return a Completable
     */
    public Completable checkMissingRequiredMetricsByResources(JsonArray resources) {
        List<Completable> completables = new ArrayList<>();
        HashSet<Long> resourceIds = new HashSet<>();
        for (Object object : resources) {
            Resource resource = ((JsonObject) object).mapTo(Resource.class);
            if (!resourceIds.contains(resource.getResourceId())) {
                completables.add(this.checkMissingRequiredPlatformMetricsByResource(resource.getResourceId()));
                resourceIds.add(resource.getResourceId());
            }
        }
        return Completable.merge(completables);
    }
}