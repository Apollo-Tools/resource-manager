package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The interface of the service proxy for the resource_ensemble entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceEnsembleService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceEnsembleService create(ResourceEnsembleRepository repository) {
        return new ResourceEnsembleServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceEnsembleService createProxy(Vertx vertx) {
        return new ResourceEnsembleServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceEnsemble.class));
    }

    /**
     * Save a new resource ensemble by its ensemble and resource.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Future that emits the persisted entity as JsonObject
     */
    Future<JsonObject> saveByEnsembleIdAndResourceId(long ensembleId, long resourceId);

    /**
     * Delete a resourc ensemble by its ensemble and resource.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return an empty Future
     */
    Future<Void> deleteByEnsembleIdAndResourceId(long ensembleId, long resourceId);

    /**
     * Check if a resource ensemble exists by its ensemble and resource.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Future that emits true if the resource ensemble exists, else false
     */
    Future<Boolean> checkExistsByEnsembleIdAndResourceId(long ensembleId, long resourceId);
}
