package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.core.json.JsonObject;

/**
 * The interface of the service proxy for the metric entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface MetricService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static MetricService create(MetricRepository metricRepository, SessionManagerProvider smProvider) {
        return new MetricServiceImpl(metricRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static MetricService createProxy(Vertx vertx) {
        return new MetricServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Metric.class));
    }

    void checkMetricTypeForSLOs(JsonObject request, Handler<AsyncResult<Void>> resultHandler);
}
