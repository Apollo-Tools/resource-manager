package at.uibk.dps.rm.handler;

import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class PrivateEntityResultHandler extends ResultHandler {

    private final ValidationHandler validationHandler;

    /**
     * Create an instance from a validation handler.
     *
     * @param validationHandler the validation handler
     */
    public PrivateEntityResultHandler(ValidationHandler validationHandler) {
        super(validationHandler);
        this.validationHandler = validationHandler;
    }

    @Override
    public Disposable handleSaveOneRequest(final RoutingContext rc) {
        return validationHandler.postOneToAccount(rc)
            .subscribe(result -> getSaveResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    @Override
    public Disposable handleDeleteRequest(final RoutingContext rc) {
        return validationHandler.deleteOneFromAccount(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc),
                throwable -> handleRequestError(rc, throwable));
    }

    @Override
    public Disposable handleFindAllRequest(RoutingContext rc) {
        return validationHandler.getAllFromAccount(rc)
            .subscribe(result -> getFindAllResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }
}
