package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;

public class FunctionHandler extends ValidationHandler {

    public FunctionHandler(FunctionService functionService) {
        super(new FunctionChecker(functionService));
    }
}
