package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;

public class FunctionChecker extends EntityChecker {
    private final FunctionService functionService;

    public FunctionChecker(FunctionService functionService) {
        super(functionService);
        this.functionService = functionService;
    }
}
