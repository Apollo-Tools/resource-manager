package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

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
    static ResourceService create(SessionManagerProvider smProvider) {
        return new ResourceServiceImpl(new ResourceRepository(), new RegionRepository(), new ResourceProviderRepository(), new MetricRepository(),
                new MetricValueRepository(), new PlatformMetricRepository(), smProvider);
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

    /**
     * Saves all resources given in TOSCA format
     *
     * @param data the resource data
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void saveStandardized(String data, Handler<AsyncResult<Void>> resultHandler);

    /**
     * returns the requested resource  in TOSCA format
     *
     * @param id the resource id
     * @param resultHandler receives the found resources as JsonObject
     */
    void getStandardized(long id, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * returns the all resource  in TOSCA format
     *
     * @param resultHandler receives the found resources as JsonArray
     */
    void getAllStandardized(Handler<AsyncResult<JsonObject>> resultHandler);

}
