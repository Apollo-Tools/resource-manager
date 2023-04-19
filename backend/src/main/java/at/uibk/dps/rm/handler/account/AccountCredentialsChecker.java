package at.uibk.dps.rm.handler.account;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.account.AccountCredentialsService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the account_credentials entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class AccountCredentialsChecker extends EntityChecker {

    private final AccountCredentialsService accountCredentialsService;

    /**
     * Create an instance from the accountCredentialsService.
     *
     * @param accountCredentialsService the account credentials service
     */
    public AccountCredentialsChecker(AccountCredentialsService accountCredentialsService) {
        super(accountCredentialsService);
        this.accountCredentialsService = accountCredentialsService;
    }

    /**
     * Check if the credentials violate uniqueness constraints.
     *
     * @param credentials the linked credentials to create
     * @param accountId the id of the account where the credentials should be stored
     * @return a Completable if does not violate uniqueness, else an AlreadyExistsException
     * gets thrown.
     */
    public Completable checkForDuplicateEntity(JsonObject credentials, long accountId) {
        Single<Boolean> existsOneByAccountAndProvider = accountCredentialsService
            .existsOneByAccountAndProvider(accountId, credentials.getJsonObject("resource_provider")
                .getLong("provider_id"));
        return ErrorHandler.handleDuplicates(existsOneByAccountAndProvider).ignoreElement();
    }

    /**
     * Find account_credentials by its credentialsId and accountId and return it, if found.
     *
     * @param credentialsId the id of the entity linked credentials
     * @param accountId the id of the owner of the credentials
     * @return a Single that emits the accountcredentials as JsonObject if found, else a
     * NotFoundException gets thrown
     */
    public Single<JsonObject> checkFindOneByCredentialsAndAccount(long credentialsId, long accountId) {
        Single<JsonObject> findOneByCredentials = accountCredentialsService
            .findOneByCredentialsAndAccount(credentialsId, accountId);
        return ErrorHandler.handleFindOne(findOneByCredentials);
    }
}
