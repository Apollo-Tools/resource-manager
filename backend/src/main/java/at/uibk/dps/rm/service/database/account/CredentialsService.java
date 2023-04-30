package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the credentials entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface CredentialsService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static CredentialsService create(CredentialsRepository credentialsRepository) {
        return new CredentialsServiceImpl(credentialsRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static CredentialsService createProxy(Vertx vertx) {
        return new CredentialsServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Credentials.class));
    }

    /**
     * Find all credentials by their creator account.
     *
     * @param accountId the id of the creator account
     * @return a Future that emits all credentials
     */
    Future<JsonArray> findAllByAccountId(long accountId);

    /**
     * Check if there exists at least one set of credentials created by the account.
     *
     * @param accountId the id of the creator account
     * @return a Future that emits true if at least one set of credentials exists, else false
     */
    Future<Boolean> existsAtLeastOneByAccount(long accountId);

    /**
     * Check if a set of credentials exists by the creator account and resource provider.
     *
     * @param accountId the id of the account
     * @param providerId the id of the resource provider
     * @return a Future that emits true if the set of credentials exists, else false
     */
    Future<Boolean> existsOnyByAccountIdAndProviderId(long accountId, long providerId);
}
