package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the credentials entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class CredentialsChecker extends EntityChecker {

    private final CredentialsService credentialsService;

    /**
     * Create an instance from the credentialsService.
     *
     * @param credentialsService the credentials service
     */
    public CredentialsChecker(CredentialsService credentialsService) {
        super(credentialsService);
        this.credentialsService = credentialsService;
    }

    public Single<JsonObject> submitCreate(long accountId, JsonObject requestBody) {
        return credentialsService.saveToAccount(accountId, requestBody);
    }

    /**
     * Find all credentials by the accountId.
     *
     * @return a Single that emits the list of found entities as JsonArray if found, else a
     * NotFoundException gets thrown
     */
    public Single<JsonArray> checkFindAll(long accountId) {
        return ErrorHandler.handleFindAll(credentialsService.findAllByAccountId(accountId));
    }

    /**
     * Check if an entity exists by the accountId and providerId.
     *
     * @param accountId the id of the account
     * @param providerId the id of the resource provider
     * @return a Completable if it exists, else a NotFoundException gets thrown
     */
    public Completable checkExistsOneByProviderId(long accountId, long providerId) {
        Single<Boolean> existsOneByProviderId = credentialsService.existsOnyByAccountIdAndProviderId(accountId,
            providerId);
        return ErrorHandler.handleCredentialsExist(existsOneByProviderId).ignoreElement();
    }
}
