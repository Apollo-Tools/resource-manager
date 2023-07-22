package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;

/**
 * Implements methods to perform CRUD operations on the vpc entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class VPCChecker extends EntityChecker {

    /**
     * Create an instance from the vpcService.
     *
     * @param vpcService the vpc service
     */
    public VPCChecker(VPCService vpcService) {
        super(vpcService);
    }
}
