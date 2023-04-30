package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.MetricType;
import at.uibk.dps.rm.repository.metric.MetricTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * The interface of the service proxy for the metric_type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface MetricTypeService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static MetricTypeService create(MetricTypeRepository metricTypeRepository) {
        return new MetricTypeServiceImpl(metricTypeRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static MetricTypeService createProxy(Vertx vertx) {
        return new MetricTypeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(MetricType.class));
    }
}
