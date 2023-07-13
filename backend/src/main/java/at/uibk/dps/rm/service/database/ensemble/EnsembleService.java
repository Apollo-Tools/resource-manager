package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

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
    static EnsembleService create(Stage.SessionFactory sessionFactory) {
        return new EnsembleServiceImpl(new EnsembleRepository(), new ResourceRepository(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static EnsembleService createProxy(Vertx vertx) {
        return new EnsembleServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Ensemble.class));
    }


    /**
     * Find all ensembles by their creator.
     *
     * @param accountId the account id of the creator
     * @return a Future that emits all ensembles as JsonArray
     */
    Future<JsonArray> findAllByAccountId(long accountId);

    /**
     * Find one ensemble by its id and creator.
     *
     * @param id the id of the ensemble
     * @param accountId the account id of the creator
     * @return a Future that emits the found ensemble if it exists, else null
     */
    Future<JsonObject> findOneByIdAndAccountId(long id, long accountId);

    /**
     * Check if an ensemble exists by its name and creator.
     *
     * @param name the name of the ensemble
     * @param accountId the account id of the creator
     * @return a Future that emits true if it exists, else false
     */
    Future<Boolean> existsOneByNameAndAccountId(String name, long accountId);

    /**
     * Update the validity of an existing ensemble.
     *
     * @param ensembleId the id of the ensemble
     * @param isValid the new validity value
     * @return an empty Future
     */
    Future<Void> updateEnsembleValidity(long ensembleId, boolean isValid);
}
