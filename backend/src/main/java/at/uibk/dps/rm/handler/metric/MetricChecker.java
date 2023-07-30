package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.metric.MetricService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the metric entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class MetricChecker extends EntityChecker {

    private final MetricService metricService;

    /**
     * Create an instance from the metricService.
     *
     * @param metricService the metric service
     */
    public MetricChecker(MetricService metricService) {
        super(metricService);
        this.metricService = metricService;
    }

    /**
     * Find all metrics by platform and if they are required or optional.
     *
     * @param platformId the id of the platform
     * @param required whether the metrics are required or optional
     * @return a Single that emits all found metrics as JsonArray
     */
    public Single<JsonArray> checkFindAllByPlatform(long platformId, boolean required) {
        Single<JsonArray> findAllByResourceTypeId = metricService.findAllByPlatformId(platformId, required);
        return ErrorHandler.handleFindAll(findAllByResourceTypeId);
    }
}
