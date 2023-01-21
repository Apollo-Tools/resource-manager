package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;

public class FunctionResourceHandler extends ValidationHandler {

    public FunctionResourceHandler(FunctionResourceService functionResourceService) {
        super(new FunctionResourceChecker(functionResourceService));
    }
}