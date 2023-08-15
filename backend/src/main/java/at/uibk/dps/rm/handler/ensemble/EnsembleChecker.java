package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
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
     * Check if a create ensemble request fulfills the service level objectives.
     *
     * @param requestBody the create ensemble request body
     * @return a Single that emits all found resources as JsonArray
     */
    public Completable checkCreateEnsembleRequestFulfillsSLOs(JsonObject requestBody) {
        return ensembleService.validateCreateEnsembleRequest(requestBody);
    }

    /**
     * Validate whether all resources of an existing ensemble fulfill its service level objectives.
     *
     * @param accountId the id of the account
     * @param ensembleId the id of the ensemble
     * @return a Single that emits a JsonArray containing every resource with its slo fulfillment state
     */
    public Single<JsonArray> checkValidateExistingEnsemble(long accountId, long ensembleId) {
        return ensembleService.validateExistingEnsemble(accountId, ensembleId);
    }

    /**
     * Validate whether all resources of all existing ensembles fulfill their service level objectives.
     *
     * @return a Completable
     */
    public Completable checkValidateAllExistingEnsembles() {
        return ensembleService.validateAllExistingEnsembles();
    }
}
