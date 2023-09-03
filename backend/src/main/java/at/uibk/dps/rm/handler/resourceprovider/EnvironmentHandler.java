package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the environment entity.
 *
 * @author matthi-g
 */
@Deprecated
public class EnvironmentHandler extends ValidationHandler {
    /**
     * Create an instance from the environmentChecker.
     *
     * @param environmentChecker the environment checker
     */
    public EnvironmentHandler(EnvironmentChecker environmentChecker) {
        super(environmentChecker);
    }
}
