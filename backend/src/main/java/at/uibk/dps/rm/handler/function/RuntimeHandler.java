package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;

public class RuntimeHandler extends ValidationHandler {
    public RuntimeHandler(RuntimeService runtimeService) {
        super(new RuntimeChecker(runtimeService));
    }
}
