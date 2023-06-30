package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

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
    static ResourceEnsembleService create(Stage.SessionFactory sessionFactory) {
        return new ResourceEnsembleServiceImpl(new ResourceEnsembleRepository(), new EnsembleSLORepository(),
            new EnsembleRepository(), new ResourceRepository(), sessionFactory);
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
     * @param accountId the id of the creator
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Future that emits the persisted entity as JsonObject
     */
    Future<JsonObject> saveByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId);

    /**
     * Delete a resource ensemble by its ensemble and resource.
     *
     * @param accountId the id of the creator
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return an empty Future
     */
    Future<Void> deleteByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId);

    /**
     * Check if a resource ensemble exists by its ensemble and resource.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Future that emits true if the resource ensemble exists, else false
     */
    Future<Boolean> checkExistsByEnsembleIdAndResourceId(long ensembleId, long resourceId);
}
