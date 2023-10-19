package at.uibk.dps.rm.service.database.util;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link SessionManagerProvider} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class SessionManagerProviderTest {

    private SessionManagerProvider sessionManagerProvider;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        sessionManagerProvider = new SessionManagerProvider(sessionFactory);
    }

    @Test
    void withTransactionSingle(VertxTestContext testContext) {
        Function<SessionManager, Single<String>> function = mock(Function.class);
        String expectedResult = "TestResult";

        when(function.apply(any(SessionManager.class))).thenReturn(Single.just(expectedResult));
        when(sessionFactory.withTransaction(any(Function.class))).thenAnswer(invocation -> {
            Function<Stage.Session, CompletionStage<String>> transactionFunction = invocation.getArgument(0);
            return transactionFunction.apply(session);
        });

        sessionManagerProvider.withTransactionSingle(function)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo("TestResult");
                testContext.completeNow();
            }), throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void withTransactionMaybe(VertxTestContext testContext) {
        Function<SessionManager, Maybe<String>> function = mock(Function.class);
        String expectedResult = "TestResult";

        when(function.apply(any(SessionManager.class))).thenReturn(Maybe.just(expectedResult));
        when(sessionFactory.withTransaction(any(Function.class))).thenAnswer(invocation -> {
            Function<Stage.Session, CompletionStage<String>> transactionFunction = invocation.getArgument(0);
            return transactionFunction.apply(session);
        });

        sessionManagerProvider.withTransactionMaybe(function)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo("TestResult");
                    testContext.completeNow();
                }), throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void withTransactionCompletable(VertxTestContext testContext) {
        Function<SessionManager, Completable> function = mock(Function.class);

        when(function.apply(any(SessionManager.class))).thenReturn(Completable.complete());
        when(sessionFactory.withTransaction(any(Function.class))).thenAnswer(invocation -> {
            Function<Stage.Session, CompletionStage<String>> transactionFunction = invocation.getArgument(0);
            return transactionFunction.apply(session);
        });

        sessionManagerProvider.withTransactionCompletable(function)
            .subscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
