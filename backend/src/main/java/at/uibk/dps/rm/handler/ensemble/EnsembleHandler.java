package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the ensemble entity.
 *
 * @author matthi-g
 */
public class EnsembleHandler extends ValidationHandler {

    private final EnsembleChecker ensembleChecker;
    /**
     * Create an instance from the ensembleChecker.
     *
     * @param ensembleChecker the ensemble checker
     */
    public EnsembleHandler(EnsembleChecker ensembleChecker) {
        super(ensembleChecker);
        this.ensembleChecker = ensembleChecker;
    }

    @Override
    public Single<JsonObject> getOneFromAccount(RoutingContext rc) {
        return super.getOneFromAccount(rc);
    }

    /**
     * Validate the resources for a create ensemble request.
     *
     * @param rc the routing context
     */
    public Completable validateNewResourceEnsembleSLOs(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return ensembleChecker.checkCreateEnsembleRequestFulfillsSLOs(requestBody);
    }

    /**
     * Validate an existing ensemble.
     *
     * @param rc the routing context
     * @return a Single that emits all resources with their validity state
     */
    public Single<JsonArray> validateExistingEnsemble(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
        .flatMap(id -> ensembleChecker.checkValidateExistingEnsemble(accountId, id));
    }

    /**
     * Validate all existing ensembles.
     */
    public void validateAllExistingEnsembles() {
        ensembleChecker.checkValidateAllExistingEnsembles()
            .subscribe();
    }
}
