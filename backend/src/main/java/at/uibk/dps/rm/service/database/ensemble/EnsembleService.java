package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.EnsembleRepositoryProvider;
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

/**
 * The interface of the service proxy for the ensemble entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface EnsembleService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static EnsembleService create(SessionManagerProvider smProvider) {
        return new EnsembleServiceImpl(new EnsembleRepositoryProvider(), smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static EnsembleService createProxy(Vertx vertx) {
        return new EnsembleServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Ensemble.class));
    }

    /**
     * Find one ensemble by its id and creator.
     *
     * @param id the id of the ensemble
     * @param accountId the account id of the creator
     * @param resultHandler receives the found ensemble as JsonObject if it exists else a
     *                      {@link at.uibk.dps.rm.exception.NotFoundException}
     */
    void findOneByIdAndAccountId(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Update the status values of a resource ensemble.
     *
     * @param ensembleId the id of the ensemble
     * @param statusValues the status values
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void updateEnsembleStatus(long ensembleId, JsonArray statusValues, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Check if all resources from an existing ensemble fulfill its service level objectives.
     *
     * @param ensembleId the id of the ensemble
     * @param resultHandler receives the found resources and their validity state as JsonArray
     */
    void validateExistingEnsemble(long accountId, long ensembleId,
        Handler<AsyncResult<JsonArray>> resultHandler);

    /**
     * Check if all resources from all existing ensembles fulfill their service level objectives.
     *
     * @param resultHandler receives nothing if the validation was successful else an error
     */
    void validateAllExistingEnsembles(Handler<AsyncResult<Void>> resultHandler);
}
