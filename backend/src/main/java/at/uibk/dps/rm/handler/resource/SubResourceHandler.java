package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class SubResourceHandler  extends ValidationHandler {

    private final ResourceChecker resourceChecker;

    /**
     * Create an instance from the resourceChecker.
     *
     * @param resourceChecker the resource checker
     */
    public SubResourceHandler(ResourceChecker resourceChecker) {
        super(resourceChecker);
        this.resourceChecker = resourceChecker;
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(resourceChecker::checkFindAllSubresources);
    }
}
