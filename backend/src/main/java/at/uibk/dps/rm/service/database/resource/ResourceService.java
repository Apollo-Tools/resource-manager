package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

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
    static ResourceService create(ResourceRepository resourceRepository) {
        return new ResourceServiceImpl(resourceRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceService createProxy(Vertx vertx) {
        return new ResourceServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Resource.class));
    }

    /**
     * Find all resources based on the metrics, regions, resource providers and resource types.
     *
     * @param metrics the names of the metrics
     * @param regionIds the ids of the regions
     * @param providerIds the ids of the resource providers
     * @param resourceTypeIds the ids of the resource types
     * @return a Future that emits all resource as JsonArray
     */
    Future<JsonArray> findAllBySLOs(List<String> metrics, List<Long> regionIds, List<Long> providerIds,
            List<Long> resourceTypeIds);

    /**
     * Find all resources where a specific function may be deployed.
     *
     * @param functionId the id of the function
     * @return a Future that emits all resources as JsonArray
     */
    Future<JsonArray> findAllByFunctionId(long functionId);

    /**
     * Check if a resource exists by its resource type.
     *
     * @param typeId the id of the resource type
     * @return a Future that emits true if it exists, else false
     */
    Future<Boolean> existsOneByResourceType(long typeId);
}
