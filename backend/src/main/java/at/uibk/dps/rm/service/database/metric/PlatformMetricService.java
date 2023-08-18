package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the resource_type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface PlatformMetricService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static PlatformMetricService create(PlatformMetricRepository repository, Stage.SessionFactory sessionFactory) {
        return new PlatformMetricServiceImpl(repository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static PlatformMetricService createProxy(Vertx vertx) {
        return new PlatformMetricServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(PlatformMetric.class));
    }

    /**
     * Find all platform metrics by a platform.
     *
     * @param platformId the id of the platform
     * @return a Future that emits all platform metrics as JsonArray
     */
    Future<JsonArray> findAllByPlatformId(long platformId);
}
