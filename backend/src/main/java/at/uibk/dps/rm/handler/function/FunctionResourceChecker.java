package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class FunctionResourceChecker  extends EntityChecker {
    private final FunctionResourceService functionResourceService;

    public FunctionResourceChecker(FunctionResourceService functionResourceService) {
        super(functionResourceService);
        this.functionResourceService = functionResourceService;
    }

    public Completable submitDeleteFunctionResource(long functionId, long resourceId) {
        return functionResourceService.deleteByFunctionAndResource(functionId, resourceId);
    }

    public Completable checkForDuplicateByFunctionAndResource(long functionId, long resourceId) {
        Single<Boolean> existsOneByFunctionAndResource = functionResourceService
            .existsOneByFunctionAndResource(functionId, resourceId);
        return ErrorHandler.handleDuplicates(existsOneByFunctionAndResource).ignoreElement();
    }

    public Completable checkFunctionResourceExistsByFunctionAndResource(long functionId, long resourceId) {
        Single<Boolean> existsOneByFunctionAndResource =
            functionResourceService.existsOneByFunctionAndResource(functionId, resourceId);
        return ErrorHandler.handleExistsOne(existsOneByFunctionAndResource).ignoreElement();
    }
}
