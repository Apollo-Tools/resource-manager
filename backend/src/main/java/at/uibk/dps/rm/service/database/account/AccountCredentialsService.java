package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the account_credentials entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface AccountCredentialsService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static AccountCredentialsService create(AccountCredentialsRepository accountCredentialsRepository,
            Stage.SessionFactory sessionFactory) {
        return new AccountCredentialsServiceImpl(accountCredentialsRepository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static AccountCredentialsService createProxy(Vertx vertx) {
        return new AccountCredentialsServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(AccountCredentials.class));
    }

    /**
     * Find account credentials by the credentials and creator account.
     *
     * @param credentialsId the id of the credentials
     * @param accountId the id of the creator account
     * @return a Future that emits the account credentials as JsonObject if they exist else null
     */
    Future<JsonObject> findOneByCredentialsAndAccount(long credentialsId, long accountId);

    /**
     * Check if account credentials exist by account and resource provider.
     *
     * @param accountId the id of the creator account
     * @param providerId the id of the resource provider
     * @return a Future that emits true if they exist, else false
     */
    Future<Boolean> existsOneByAccountAndProvider(long accountId, long providerId);
}
