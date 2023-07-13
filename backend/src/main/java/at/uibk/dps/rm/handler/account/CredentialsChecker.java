package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

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

    /**
     * Find all credentials by the accountId and include the secrets if includeSecrets is true.
     *
     * @return a Single that emits the list of found entities as JsonArray if found
     */
    public Single<JsonArray> checkFindAll(long accountId, boolean includeSecrets) {
        return ErrorHandler.handleFindAll(credentialsService
            .findAllByAccountIdAndIncludeExcludeSecrets(accountId, includeSecrets));
    }

    @Override
    public Single<JsonArray> checkFindAll(long accountId) {
        return checkFindAll(accountId, false);
    }

    /**
     * Check if an entity exists by the accountId and providerId.
     *
     * @param accountId the id of the account
     * @param providerId the id of the resource provider
     * @return a Completable if it exists, else a NotFoundException gets thrown
     */
    public Completable checkExistsOneByProviderId(long accountId, long providerId) {
        Single<Boolean> existsOneByProviderId = credentialsService.existsOneByAccountIdAndProviderId(accountId,
            providerId);
        return ErrorHandler.handleCredentialsExist(existsOneByProviderId).ignoreElement();
    }
}
