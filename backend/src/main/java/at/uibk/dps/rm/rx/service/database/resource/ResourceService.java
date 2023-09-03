package at.uibk.dps.rm.rx.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.rx.repository.metric.MetricRepository;
import at.uibk.dps.rm.rx.repository.resource.ResourceRepository;
import at.uibk.dps.rm.rx.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.rx.service.ServiceProxyAddress;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
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
     * @param resultHandler receives the found resources as JsonArray
     */
    void findAllBySLOs(JsonObject data, Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Find all sub resources by their main resource.
     *
     * @param resourceId the id of the main resource
     * @param resultHandler receives the found sub entities as JsonArray
     */
    void findAllSubResources(long resourceId, Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Find all resources by resourceIds.
     *
     * @param resourceIds the list of resource ids
     * @param resultHandler receives the found resources as JsonArray
     */
    void findAllByResourceIds(List<Long> resourceIds, Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Update a cluster resource using the contents of the data object.
     *
     * @param resourceName the name of the cluster resource
     * @param data the monitoring data
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void updateClusterResource(String resourceName, K8sMonitoringData data, Handler<AsyncResult<Void>> resultHandler);
}
