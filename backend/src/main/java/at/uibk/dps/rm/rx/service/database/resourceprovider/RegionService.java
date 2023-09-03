package at.uibk.dps.rm.rx.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.rx.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.rx.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.rx.service.ServiceProxyAddress;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the region entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface RegionService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static RegionService create(Stage.SessionFactory sessionFactory) {
        return new RegionServiceImpl(new RegionRepository(), new ResourceProviderRepository(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static RegionService createProxy(Vertx vertx) {
        return new RegionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Region.class));
    }

    /**
     * Find all regions that belong to the resource provider.
     *
     * @param providerId the id of the resource provider
     * @param resultHandler receives the found regions as JsonArray
     */
    void findAllByProviderId(long providerId, Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Find all regions by platform.
     *
     * @param platformId the id of the platform
     * @param resultHandler receives the found regions as JsonArray
     */
    void findAllByPlatformId(long platformId, Handler<AsyncResult<JsonArray>> resultHandler);

}
