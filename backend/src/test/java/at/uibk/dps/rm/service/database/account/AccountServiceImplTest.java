package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.dto.account.NewAccountDTO;
import at.uibk.dps.rm.entity.dto.account.RoleEnum;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Role;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.account.AccountRepository;
import at.uibk.dps.rm.repository.account.RoleRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.util.misc.PasswordUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link AccountServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;
    
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        accountService = new AccountServiceImpl(accountRepository, roleRepository, sessionFactory);
    }

    @Test
    void loginAccount(VertxTestContext testContext) {
        long accountId = 1L;
        String username = "user1", password = "pw1";
        String hashedPw = new PasswordUtility().hashPassword(password.toCharArray());
        Account user = TestAccountProvider.createAccount(accountId, username, hashedPw);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findByUsername(sessionManager, username))
            .thenReturn(Maybe.just(user));
        
        accountService.loginAccount(username, password, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.getLong("account_id")).isEqualTo(1L);
            assertThat(result.getString("username")).isEqualTo("user1");
            assertThat(result.getString("password")).isNull();
            testContext.completeNow();
        })));
    }

    @Test
    void loginAccountInvalidPW(VertxTestContext testContext) {
        long accountId = 1L;
        String username = "user1", password = "pw1";
        String hashedPw = new PasswordUtility().hashPassword(password.toCharArray()) + 2;
        Account user = TestAccountProvider.createAccount(accountId, username, hashedPw);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findByUsername(sessionManager, username))
            .thenReturn(Maybe.just(user));

        accountService.loginAccount(username, password, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void loginAccountNotFound(VertxTestContext testContext) {
        String username = "user1", password = "pw1";

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findByUsername(sessionManager, username))
            .thenReturn(Maybe.empty());

        accountService.loginAccount(username, password, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                assertThat(throwable.getMessage()).isEqualTo("invalid credentials");
                testContext.completeNow();
            })));
    }

    @Test
    void save(VertxTestContext testContext) {
        String username = "user1", password = "pw1";
        NewAccountDTO accountDTO = new NewAccountDTO();
        accountDTO.setUsername(username);
        accountDTO.setPassword(password);
        Role role = TestAccountProvider.createRoleDefault();

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findByUsername(sessionManager, username))
            .thenReturn(Maybe.empty());
        when(roleRepository.findByRoleName(sessionManager, RoleEnum.DEFAULT.getValue()))
            .thenReturn(Maybe.just(role));
        when(session.persist(any(Account.class))).thenReturn(CompletionStages.voidFuture());

        accountService.save(JsonObject.mapFrom(accountDTO), testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("account_id")).isNull();
                assertThat(result.getString("username")).isEqualTo("user1");
                assertThat(result.containsKey("password")).isFalse();
                assertThat(result.getJsonObject("role").getString("role")).isEqualTo("default");
                testContext.completeNow();
            })));
    }

    @Test
    void saveAlreadyExists(VertxTestContext testContext) {
        String username = "user1", password = "pw1";
        NewAccountDTO accountDTO = new NewAccountDTO();
        accountDTO.setUsername(username);
        accountDTO.setPassword(password);
        Account account = TestAccountProvider.createAccount(1L, username, password);
        Role role = TestAccountProvider.createRoleDefault();

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findByUsername(sessionManager, username))
            .thenReturn(Maybe.just(account));
        when(roleRepository.findByRoleName(sessionManager, RoleEnum.DEFAULT.getValue()))
            .thenReturn(Maybe.just(role));


        accountService.save(JsonObject.mapFrom(accountDTO), testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                assertThat(throwable.getMessage()).isEqualTo("Account already exists");
                testContext.completeNow();
            })));
    }

    @Test
    void update(VertxTestContext testContext) {
        PasswordUtility pwUtility = new PasswordUtility();
        long accountId = 1L;
        String username = "user1", password = "pw1";
        String hashedPw = pwUtility.hashPassword(password.toCharArray());
        Account entity = TestAccountProvider.createAccount(accountId, username, hashedPw);
        JsonObject fields = new JsonObject("{\"old_password\": \"pw1\", \"new_password\": \"pw2\"}");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findById(sessionManager, accountId))
            .thenReturn(Maybe.just(entity));

        accountService.update(accountId, fields, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                assertThat(pwUtility.verifyPassword(entity.getPassword(), "pw2".toCharArray()))
                    .isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void updateInvalidPassword(VertxTestContext testContext) {
        long accountId = 1L;
        String username = "user1", password = "pw1";
        Account entity = TestAccountProvider.createAccount(accountId, username, password);
        JsonObject fields = new JsonObject("{\"old_password\": \"pw2\", \"new_password\": \"pw2\"}");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findById(sessionManager, accountId)).thenReturn(Maybe.just(entity));

        accountService.update(accountId, fields, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(UnauthorizedException.class);
                assertThat(throwable.getMessage()).isEqualTo("old password is invalid");
                testContext.completeNow();
            })));
    }

    @Test
    void updateNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        JsonObject fields = new JsonObject("{\"old_password\": \"pw1\", \"new_password\": \"pw2\"}");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(accountRepository.findById(sessionManager, accountId)).thenReturn(Maybe.empty());

        accountService.update(accountId, fields, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("Account not found");
                testContext.completeNow();
            })));
    }
}
