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
     * Check if a create ensemble request fulfills the service level objectives.
     *
     * @param requestBody the create ensemble request body
     * @return a Single that emits all found resources as JsonArray
     */
    public Completable checkCreateEnsembleRequestFulfillsSLOs(JsonObject requestBody) {
        return ensembleService.validateCreateEnsembleRequest(requestBody);
    }

    public Single<JsonArray> checkValidateExistingEnsemble(long accountId, long ensembleId) {
        return ensembleService.validateExistingEnsemble(accountId, ensembleId);
    }

    public Completable checkValidateAllExistingEnsembles() {
        return ensembleService.validateAllExistingEnsembles();
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
}
