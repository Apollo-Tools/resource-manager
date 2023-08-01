package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.repository.account.AccountCredentialsRepository;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.hibernate.reactive.stage.Stage;

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
    static CredentialsService create(Stage.SessionFactory sessionFactory) {
        return new CredentialsServiceImpl(new CredentialsRepository(), new AccountRepository(),
            new AccountCredentialsRepository(), new ResourceProviderRepository(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static CredentialsService createProxy(Vertx vertx) {
        return new CredentialsServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Credentials.class));
    }

    /**
     * Find all credentials by their creator account and include/exclude the secrets.
     *
     * @param accountId the id of the creator account
     * @param includeSecrets whether to include or exclude the secrets
     * @return a Future that emits all credentials
     */
    Future<JsonArray> findAllByAccountIdAndIncludeExcludeSecrets(long accountId, boolean includeSecrets);
}
