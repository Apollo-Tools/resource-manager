package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;

public class FunctionResourceChecker  extends EntityChecker {
    private final FunctionResourceService functionResourceService;

    public FunctionResourceChecker(FunctionResourceService functionResourceService) {
        super(functionResourceService);
        this.functionResourceService = functionResourceService;
    }
}
