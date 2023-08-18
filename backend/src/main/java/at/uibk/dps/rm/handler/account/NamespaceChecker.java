package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.NamespaceService;

/**
 * Implements methods to perform CRUD operations on the k8s_namespace entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class NamespaceChecker extends EntityChecker {

    /**
     * Create an instance from the namespaceService.
     *
     * @param namespaceService the namespace service
     */
    public NamespaceChecker(NamespaceService namespaceService) {
        super(namespaceService);
    }
}
