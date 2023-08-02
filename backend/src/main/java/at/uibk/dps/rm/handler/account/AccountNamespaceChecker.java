package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountNamespaceService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the account_namespace entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class AccountNamespaceChecker extends EntityChecker {

    private final AccountNamespaceService service;
    /**
     * Create an instance from the service.
     *
     * @param service the account namespace service to use
     */
    public AccountNamespaceChecker(AccountNamespaceService service) {
        super(service);
        this.service = service;
    }

    /**
     * Submit the creation of a new account namespace by the accountId and namespaceId.
     *
     * @param accountId the id of the creator
     * @param namespaceId the id of the namespace
     * @return a Single that emits the persisted entity
     */
    public Single<JsonObject> submitCreate(long accountId, long namespaceId) {
        return service.saveByAccountIdAndNamespaceId(accountId, namespaceId);
    }

    /**
     * Submit the deletion of a resource ensemble by its ensembleId and resourceId
     *
     * @param accountId the id of the creator
     * @param namespaceId the id of the namespace
     * @return a Completable
     */
    public Completable submitDelete(long accountId, long namespaceId) {
        return service.deleteByAccountIdAndNamespaceId(accountId, namespaceId);
    }
}
