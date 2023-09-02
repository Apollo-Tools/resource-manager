package at.uibk.dps.rm.rx.handler.artifact;

import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.artifact.FunctionTypeService;

/**
 * Processes the http requests that concern the function artifact type entity.
 *
 * @author matthi-g
 */
public class FunctionTypeHandler extends ValidationHandler {
    /**
     * Create an instance from the functionTypeChecker.
     *
     * @param functionTypeService the service
     */
    public FunctionTypeHandler(FunctionTypeService functionTypeService) {
        super(functionTypeService);
    }
}
