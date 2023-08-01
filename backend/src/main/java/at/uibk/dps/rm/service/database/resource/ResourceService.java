package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.List;

/**
 * The interface of the service proxy for the resource entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceService create(SessionFactory sessionFactory) {
        return new ResourceServiceImpl(new ResourceRepository(), new RegionRepository(), new MetricRepository(),
            sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceService createProxy(Vertx vertx) {
        return new ResourceServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Resource.class));
    }

    /**
     * Find all resources by service level objectives.
     *
     * @param data the data containing all service level objectives
     * @return a Future that emits all resources as JsonArray
     */
    Future<JsonArray> findAllBySLOs(JsonObject data);

    Future<JsonArray> findAllSubResources(long resourceId);

    /**
     * Find all resources by resourceIds.
     *
     * @param resourceIds the list of resource ids
     * @return a Future that emits all resources as JsonArray
     */
    Future<JsonArray> findAllByResourceIds(List<Long> resourceIds);
}
