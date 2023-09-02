package at.uibk.dps.rm.rx.handler.function;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.function.RuntimeService;

/**
 * Processes the http requests that concern the runtime entity.
 *
 * @author matthi-g
 */
public class RuntimeHandler extends ValidationHandler {

    /**
     * Create an instance from the runtimeService.
     *
     * @param runtimeService the service
     */
    public RuntimeHandler(RuntimeService runtimeService) {
        super(runtimeService);
    }
}
