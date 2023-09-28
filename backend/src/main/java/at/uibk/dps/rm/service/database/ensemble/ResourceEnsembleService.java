package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

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
    static ResourceEnsembleService create(SessionManagerProvider smProvider) {
        return new ResourceEnsembleServiceImpl(new EnsembleRepositoryProvider(), smProvider);
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
     * @param resultHandler receives the persisted entity if the save process was successful else
     *                      it receives an error
     */
    void saveByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId,
        Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Delete a resource ensemble by its ensemble and resource.
     *
     * @param accountId the id of the creator
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @param resultHandler receives nothing if the deletion was successful else an error
     */
    void deleteByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId,
        Handler<AsyncResult<Void>> resultHandler);
}
