package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class EnsembleHandler extends ValidationHandler {

    private final EnsembleChecker ensembleChecker;

    private final EnsembleSLOChecker ensembleSLOChecker;

    /**
     * Create an instance from the ensembleChecker.
     *
     * @param ensembleChecker the ensemble checker
     */
    public EnsembleHandler(EnsembleChecker ensembleChecker, EnsembleSLOChecker ensembleSLOChecker) {
        super(ensembleChecker);
        this.ensembleChecker = ensembleChecker;
        this.ensembleSLOChecker = ensembleSLOChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        CreateEnsembleRequest ensembleRequest = rc.body().asJsonObject().mapTo(CreateEnsembleRequest.class);
        long accountId = rc.user().principal().getLong("account_id");
        Account createdBy = new Account();
        createdBy.setAccountId(accountId);

        Ensemble ensemble = new Ensemble();
        ensemble.setName(ensembleRequest.getName());
        ensemble.setCreatedBy(createdBy);


        return entityChecker.submitCreate(JsonObject.mapFrom(ensemble));
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> ensembleChecker.checkFindOne(id, rc.user().principal().getLong("account_id")));
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return ensembleChecker.checkFindAll(accountId);
    }

    @Override
    protected Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> ensembleChecker.checkFindOne(id, rc.user().principal().getLong("account_id"))
                .flatMapCompletable(this::checkDeleteEntityIsUsed)
                .andThen(Single.just(id)))
            .flatMapCompletable(entityChecker::submitDelete);
    }
}
