package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the runtime entity.
 *
 * @author matthi-g
 */
public class RuntimeHandler extends ValidationHandler {

    /**
     * Create an instance from the runtimeChecker and fileSystemChecker
     *
     * @param runtimeChecker the runtime checker
     */
    public RuntimeHandler(RuntimeChecker runtimeChecker) {
        super(runtimeChecker);
    }
}
