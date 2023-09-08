package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.AccountNamespace;
import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.account.AccountNamespaceRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    private SessionManager sessionManager;

    private long accountId, namespaceId;
    private Account account;
    private K8sNamespace namespace;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountNamespaceService = new AccountNamespaceServiceImpl(accountNamespaceRepository, sessionFactory);
        accountId = 1L;
        namespaceId = 2L;
        account = TestAccountProvider.createAccount(accountId);
        namespace = TestResourceProviderProvider.createNamespace(namespaceId);
        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
    }

    @Test
    void saveByAccountIdAndNamespaceId(VertxTestContext testContext) {
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(session.find(K8sNamespace.class, namespaceId)).thenReturn(CompletionStages.completedFuture(namespace));
        when(accountNamespaceRepository.findByAccountIdAndResourceId(sessionManager, accountId,
            namespace.getResource().getResourceId())).thenReturn(Single.just(List.of()));
        when(session.find(Account.class, accountId)).thenReturn(CompletionStages.completedFuture(account));
        when(session.persist(any(AccountNamespace.class))).thenReturn(CompletionStages.voidFuture());

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("account_id")).isEqualTo(1L);
                assertThat(result.getLong("namespace_id")).isEqualTo(2L);
                testContext.completeNow();
        })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdAccountNotFound(VertxTestContext testContext) {
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(session.find(K8sNamespace.class, namespaceId)).thenReturn(CompletionStages.completedFuture(namespace));
        when(accountNamespaceRepository.findByAccountIdAndResourceId(sessionManager, accountId,
            namespace.getResource().getResourceId())).thenReturn(Single.just(List.of()));
        when(session.find(Account.class, accountId)).thenReturn(CompletionStages.nullFuture());

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdAlreadyExistsForResource(VertxTestContext testContext) {
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(session.find(K8sNamespace.class, namespaceId)).thenReturn(CompletionStages.completedFuture(namespace));
        when(accountNamespaceRepository.findByAccountIdAndResourceId(sessionManager, accountId,
            namespace.getResource().getResourceId())).thenReturn(Single.just(List.of(new AccountNamespace())));

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                assertThat(throwable.getMessage()).isEqualTo("only one namespace per resource allowed");
                testContext.completeNow();
            })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdNamespaceNotFound(VertxTestContext testContext) {
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());
        when(session.find(K8sNamespace.class, namespaceId)).thenReturn(CompletionStages.nullFuture());

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveByAccountIdAndNamespaceIdAlreadyExists(VertxTestContext testContext) {
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.just(new AccountNamespace()));
        when(session.find(K8sNamespace.class, namespaceId)).thenReturn(CompletionStages.nullFuture());

        accountNamespaceService.saveByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void deleteByAccountIdAndNamespaceId(VertxTestContext testContext) {
        AccountNamespace accountNamespace = TestAccountProvider.createAccountNamespace(1L, account, namespace);
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.just(accountNamespace));
        when(session.remove(accountNamespace)).thenReturn(CompletionStages.voidFuture());

        accountNamespaceService.deleteByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
    }

    @Test
    void deleteByAccountIdAndNamespaceIdNotFound(VertxTestContext testContext) {
        when(accountNamespaceRepository.findByAccountIdAndNamespaceId(sessionManager, accountId, namespaceId))
            .thenReturn(Maybe.empty());

        accountNamespaceService.deleteByAccountIdAndNamespaceId(accountId, namespaceId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }
}
