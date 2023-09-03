package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;

/**
 * Processes the http requests that concern the vpc entity.
 *
 * @author matthi-g
 */
@Deprecated
public class VPCHandler extends ValidationHandler {
    /**
     * Create an instance from the vpcChecker.
     *
     * @param vpcChecker the vpc checker
     */
    public VPCHandler(VPCChecker vpcChecker) {
        super(vpcChecker);
    }
}
