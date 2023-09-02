package at.uibk.dps.rm.handler.artifact;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the function artifact type entity.
 *
 * @author matthi-g
 */
@Deprecated
public class FunctionTypeHandler extends ValidationHandler {
    /**
     * Create an instance from the functionTypeChecker.
     *
     * @param functionTypeChecker the artifact type checker
     */
    public FunctionTypeHandler(FunctionTypeChecker functionTypeChecker) {
        super(functionTypeChecker);
    }
}
