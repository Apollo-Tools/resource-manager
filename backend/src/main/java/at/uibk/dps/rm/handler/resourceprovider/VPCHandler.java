package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;

/**
 * Processes the http requests that concern the vpc entity.
 *
 * @author matthi-g
 */
public class VPCHandler extends ValidationHandler {
    /**
     * Create an instance from the vpcService.
     *
     * @param vpcService the service
     */
    public VPCHandler(VPCService vpcService) {
        super(vpcService);
    }
}
