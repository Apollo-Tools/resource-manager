package at.uibk.dps.rm.handler;

import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * This class is used to handle the different types of requests, that the API can receive.
 * - FIND ONE
 * - FIND ALL
 * - POST ONE
 * - POST ALL
 * - UPDATE ONE
 * - DELETE ONE
 * The difference to the {@link ResultHandler} is, that all methods only work on the scope of
 * user created entities.
 *
 * @author matthi-g
 */
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
    public void handleSaveOneRequest(final RoutingContext rc) {
        validationHandler.postOneToAccount(rc)
            .subscribe(result -> getSaveResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    @Override
    public void handleFindOneRequest(RoutingContext rc) {
        validationHandler.getOneFromAccount(rc)
            .subscribe(result -> getFindOneResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }

    @Override
    public void handleDeleteRequest(final RoutingContext rc) {
        validationHandler.deleteOneFromAccount(rc)
            .subscribe(() -> getSaveAllUpdateDeleteResponse(rc),
                throwable -> handleRequestError(rc, throwable));
    }

    @Override
    public void handleFindAllRequest(RoutingContext rc) {
        validationHandler.getAllFromAccount(rc)
            .subscribe(result -> getFindAllResponse(rc, result),
                throwable -> handleRequestError(rc, throwable));
    }
}
