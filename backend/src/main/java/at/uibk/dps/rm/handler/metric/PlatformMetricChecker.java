package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.metric.PlatformMetricService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the platform_metric entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
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
     * Find all metrics that are supported by a platform.
     *
     * @param platformId the id of the platform
     * @return a Single that emits all found metrics as a JsonArray
     */
    public Single<JsonArray> checkFindAllByPlatformId(long platformId) {
        return service.findAllByPlatformId(platformId);
    }
}
