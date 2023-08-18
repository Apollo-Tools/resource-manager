package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link FunctionServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionServiceImplTest {

    private FunctionService functionService;

    @Mock
    private FunctionRepository functionRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @RegisterExtension
    public static final RunTestOnContext rtoc = new RunTestOnContext();

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        JsonMapperConfig.configJsonMapper();
        functionService = new FunctionServiceImpl(functionRepository, sessionFactory);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long functionId = 1L;
        Function entity = TestFunctionProvider.createFunction(functionId, "func","true");

        SessionMockHelper.mockSession(sessionFactory, session);
        when(functionRepository.findByIdAndFetch(session, functionId))
            .thenReturn(CompletionStages.completedFuture(entity));

        functionService.findOne(functionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("function_id")).isEqualTo(1L);
                assertThat(result.getString("code")).isEqualTo("true");
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long functionId = 1L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(functionRepository.findByIdAndFetch(session, functionId))
            .thenReturn(CompletionStages.completedFuture(null));

        functionService.findOne(functionId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Function f1 = TestFunctionProvider.createFunction(1L, "func1", "true");
        Function f2 = TestFunctionProvider.createFunction(2L, "func2", "false");

        SessionMockHelper.mockSession(sessionFactory, session);
        when(functionRepository.findAllAndFetch(session))
            .thenReturn(CompletionStages.completedFuture(List.of(f1, f2)));

        functionService.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("function_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("function_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
