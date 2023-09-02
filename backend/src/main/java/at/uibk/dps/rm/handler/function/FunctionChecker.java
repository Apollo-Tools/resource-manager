package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the function entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
@Deprecated
public class FunctionChecker extends EntityChecker {

    private final FunctionService service;
    /**
     * Create an instance from the functionService.
     *
     * @param functionService the function service
     */
    public FunctionChecker(FunctionService functionService) {
        super(functionService);
        this.service = functionService;
    }

    /**
     * Find all functions that are accessible by the account.
     *
     * @param accountId the id of the account
     * @return a Single that emits the list of found functions as JsonArray
     */
    public Single<JsonArray> checkFindAllAccessible(long accountId) {
        return ErrorHandler.handleFindAll(service.findAllAccessibleFunctions(accountId));
    }
}
