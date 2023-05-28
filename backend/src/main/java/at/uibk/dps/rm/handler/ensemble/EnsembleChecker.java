package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the ensemble entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class EnsembleChecker extends EntityChecker {

    private final EnsembleService ensembleService;

    /**
     * Create an instance from the ensembleService.
     *
     * @param ensembleService the ensemble service to use
     */
    public EnsembleChecker(EnsembleService ensembleService) {
        super(ensembleService);
        this.ensembleService = ensembleService;
    }

    /**
     * Find the ensemble by its id and account id and return it, if found.
     *
     * @param id the id of the entity
     * @param accountId the id of the account
     * @return a Single that emits the ensemble as JsonObject if found, else a NotFoundException
     * gets thrown
     */
    public Single<JsonObject> checkFindOne(long id, long accountId) {
        Single<JsonObject> findOneById = ensembleService.findOneByIdAndAccountId(id, accountId);
        return ErrorHandler.handleFindOne(findOneById);
    }

    /**
     * Check if an ensemble exists by its name and account id.
     *
     * @param name the name of the ensemble
     * @param accountId the id of the account
     * @return a Completable if the ensemble does not exist, else an AlreadyExistsException
     * gets thrown
     */
    public Completable checkExistsOneByName(String name, long accountId) {
        Single<Boolean> existsOneByName = ensembleService.existsOneByNameAndAccountId(name, accountId);
        return ErrorHandler.handleDuplicates(existsOneByName).ignoreElement();
    }

    /**
     * Find all ensembles by account.
     *
     * @param accountId the id of the account
     * @return a Single that emits all found ensembles as JsonArray
     */
    public Single<JsonArray> checkFindAll(long accountId) {
        return ErrorHandler.handleFindAll(ensembleService.findAllByAccountId(accountId));
    }

    /**
     * Update the validity value of an ensemble by its id.
     *
     * @param ensembleId the id of the ensemble
     * @param validity the new validity value
     * @return a Completable
     */
    public Completable submitUpdateValidity(long ensembleId, boolean validity) {
        return ensembleService.updateEnsembleValidity(ensembleId, validity);
    }
}
