package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.AccountNamespace;
import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link AccountNamespaceServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountNamespaceServiceTest {

    private AccountNamespaceService accountNamespaceService;

    @Mock
    private AccountNamespaceRepository accountNamespaceRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private long accountId, namespaceId;
    private Account account;
    private K8sNamespace namespace;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountNamespaceService = new AccountNamespaceServiceImpl(accountNamespaceRepository, smProvider);
        accountId = 1L;
        namespaceId = 2L;
        account = TestAccountProvider.createAccount(accountId);
        namespace = TestResourceProviderProvider.createNamespace(namespaceId);
    }

    @Test
    void saveByAccountIdAndNamespaceId(VertxTestContext testContext) {
        AccountNamespace an = TestAccountProvider.createAccountNamespace(1L, accountId, namespaceId);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(K8sNamespace.class, namespaceId)).thenReturn(Maybe.just(namespace));
        when(accountNamespaceRepository.findByAccountIdAndResourceId(sessionManager, accountId,
            namespace.getResource().getResourceId())).thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.just(account));
        when(sessionManager.persist(argThat((AccountNamespace persist) ->
                persist.getNamespace().equals(namespace) && persist.getAccount().equals(account))))
            .thenReturn(Single.just(an));

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("account_id")).isEqualTo(1L);
                assertThat(result.getLong("namespace_id")).isEqualTo(2L);
                testContext.completeNow();
        })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdAccountNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(K8sNamespace.class, namespaceId)).thenReturn(Maybe.just(namespace));
        when(accountNamespaceRepository.findByAccountIdAndResourceId(sessionManager, accountId,
            namespace.getResource().getResourceId())).thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.empty());

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdAlreadyExistsForResource(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(K8sNamespace.class, namespaceId)).thenReturn(Maybe.just(namespace));
        when(accountNamespaceRepository.findByAccountIdAndResourceId(sessionManager, accountId,
            namespace.getResource().getResourceId())).thenReturn(Maybe.just(new AccountNamespace()));
        when(sessionManager.find(Account.class, accountId)).thenReturn(Maybe.just(account));

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                assertThat(throwable.getMessage()).isEqualTo("only one namespace per resource allowed");
                testContext.completeNow();
            })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdNamespaceNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(K8sNamespace.class, namespaceId)).thenReturn(Maybe.empty());
        doReturn(Maybe.just(account)).when(sessionManager).find(Account.class, accountId);

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.just(new AccountNamespace()));
        when(sessionManager.find(K8sNamespace.class, namespaceId)).thenReturn(Maybe.empty());
        doReturn(Maybe.just(account)).when(sessionManager).find(Account.class, accountId);

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void deleteByAccountIdAndNamespaceId(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        AccountNamespace accountNamespace = TestAccountProvider.createAccountNamespace(1L, account, namespace);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.just(accountNamespace));
        when(sessionManager.remove(accountNamespace)).thenReturn(Completable.complete());

        accountNamespaceService.deleteByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
    }

    @Test
    void deleteByAccountIdAndNamespaceIdNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());

        accountNamespaceService.deleteByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }
}
