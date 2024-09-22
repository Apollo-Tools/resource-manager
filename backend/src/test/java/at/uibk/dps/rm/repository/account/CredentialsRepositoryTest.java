package at.uibk.dps.rm.repository.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link CredentialsRepository} class.
 *
 * @author matthi-g
 */
public class CredentialsRepositoryTest extends DatabaseTest {

    private final CredentialsRepository repository = new CredentialsRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            ResourceProvider rpAWS = TestResourceProviderProvider.createResourceProvider(1L);
            ResourceProvider rpCustomCloud = TestResourceProviderProvider.createResourceProvider(4L);
            Credentials c1 = TestAccountProvider.createCredentials(null, rpAWS);
            Credentials c2 = TestAccountProvider.createCredentials(null, rpCustomCloud);
            Credentials c3 = TestAccountProvider.createCredentials(null, rpCustomCloud);
            AccountCredentials ac1 = TestAccountProvider.createAccountCredentials(null, accountAdmin,
                c1);
            AccountCredentials ac2 = TestAccountProvider.createAccountCredentials(null, accountDefault,
                c2);
            AccountCredentials ac3 = TestAccountProvider.createAccountCredentials(null, accountAdmin,
                c3);
            return sessionManager.persist(c1)
                .flatMap(res -> sessionManager.persist(c2))
                .flatMap(res -> sessionManager.persist(c3))
                .flatMap(res -> sessionManager.persist(ac1))
                .flatMap(res -> sessionManager.persist(ac2))
                .flatMap(res -> sessionManager.persist(ac3));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, true",
        "4, false"
    })
    void findByIdAndFetch(long credentialsId, boolean exists, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, credentialsId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getCredentialsId()).isEqualTo(credentialsId);
                    assertThat(result.getResourceProvider().getProvider()).isEqualTo("aws");
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true",
        "2, 1, false",
        "1, 2, false",
        "2, 2, true"
    })
    void findByIdAndAccountId(long credentialsId, long accountId, boolean exists, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
            .findByIdAndAccountId(sessionManager, credentialsId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getCredentialsId()).isEqualTo(credentialsId);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByAccountId(sessionManager, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getCredentialsId()).isEqualTo(1L);
                assertThat(result.get(0).getResourceProvider().getProvider()).isEqualTo("aws");
                assertThat(result.get(1).getCredentialsId()).isEqualTo(3L);
                assertThat(result.get(1).getResourceProvider().getProvider()).isEqualTo("custom-fog");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has not thrown exception"));
    }



    @ParameterizedTest
    @CsvSource({
        "1, 1, true, 1",
        "2, 4, true, 2",
        "1, 4, true, 3",
        "1, 3, false, -1",
        "2, 1, false, -1"
    })
    void findByAccountIdAndProviderId(long accountId, long providerId, boolean exists, long credentialsId,
            VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
            .findByAccountIdAndProviderId(sessionManager, accountId, providerId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getCredentialsId()).isEqualTo(credentialsId);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }
}
