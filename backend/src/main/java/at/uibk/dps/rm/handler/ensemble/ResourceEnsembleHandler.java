package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resources linked to the ensemble entity.
 *
 * @author matthi-g
 */
public class ResourceEnsembleHandler extends ValidationHandler {

    private final ResourceEnsembleChecker resourceEnsembleChecker;

    /**
     * Create an instance from the resourceEnsembleChecker.
     *
     * @param resourceEnsembleChecker the resource ensemble checker
     */
    public ResourceEnsembleHandler(ResourceEnsembleChecker resourceEnsembleChecker) {
        super(resourceEnsembleChecker);
        this.resourceEnsembleChecker = resourceEnsembleChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "ensembleId")
            .flatMap(ensembleId -> HttpHelper.getLongPathParam(rc, "resourceId")
                .flatMap(resourceId -> resourceEnsembleChecker.submitCreate(accountId, ensembleId, resourceId))
            );
    }

    @Override
    protected Completable deleteOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "ensembleId")
            .flatMapCompletable(ensembleId -> HttpHelper.getLongPathParam(rc, "resourceId")
                .flatMapCompletable(resourceId -> resourceEnsembleChecker.submitDelete(accountId, ensembleId,
                    resourceId))
            );
    }
}
