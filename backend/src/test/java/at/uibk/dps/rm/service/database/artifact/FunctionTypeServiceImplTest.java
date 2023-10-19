package at.uibk.dps.rm.service.database.artifact;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.repository.artifact.FunctionTypeRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionTypeServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionTypeServiceImplTest {

    private FunctionTypeService functionTypeService;

    @Mock
    private FunctionTypeRepository functionTypeRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private FunctionType functionType;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionTypeService = new FunctionTypeServiceImpl(functionTypeRepository, smProvider);
        functionType = TestFunctionProvider.createFunctionType(1L, "fooType");
    }

    @Test
    void save(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(functionTypeRepository.findByName(sessionManager, functionType.getName())).thenReturn(Maybe.empty());
        when(sessionManager.persist(functionType)).thenReturn(Single.just(functionType));
        functionTypeService.save(JsonObject.mapFrom(functionType),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("artifact_type_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void saveAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(functionTypeRepository.findByName(sessionManager, functionType.getName()))
            .thenReturn(Maybe.just(functionType));
        when(sessionManager.persist(functionType)).thenReturn(Single.just(functionType));
        functionTypeService.save(JsonObject.mapFrom(functionType),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            })));
    }
}
