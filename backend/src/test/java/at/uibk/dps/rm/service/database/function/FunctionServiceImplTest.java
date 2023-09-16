package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.entity.dto.function.UpdateFunctionDTO;
import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.misc.UploadFileHelper;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Context context;

    @Mock
    private Vertx vertx;

    private long functionId, accountId;
    private Account account;
    private Runtime rPython, rJava;
    private FunctionType ft1;
    private Function f1Code, f2Code, f1File;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionService = new FunctionServiceImpl(functionRepository, smProvider);
        accountId = 1L;
        functionId = 2L;
        account = TestAccountProvider.createAccount(accountId);
        rPython = TestFunctionProvider.createRuntime(1L, RuntimeEnum.PYTHON38.getValue());
        rJava = TestFunctionProvider.createRuntime(2L, RuntimeEnum.JAVA11.getValue());
        ft1 = TestFunctionProvider.createFunctionType(11L, "ftype");
        f1Code = TestFunctionProvider.createFunction(functionId, ft1, "func1", "true",
            rPython, false, 200, 1024, account);
        f2Code = TestFunctionProvider.createFunction(functionId + 1, ft1, "func2",
            "false", rJava, false, 150, 512, account);
        f1File = TestFunctionProvider.createFunction(functionId + 1, ft1, "func3",
            "file-name-id-00", rPython, true, 150, 512, account);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(functionRepository.findByIdAndFetch(sessionManager, functionId)).thenReturn(Maybe.just(f1Code));

        functionService.findOne(functionId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("function_id")).isEqualTo(2L);
                assertThat(result.getString("code")).isEqualTo("true");
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(functionRepository.findByIdAndFetch(sessionManager, functionId)).thenReturn(Maybe.empty());

        functionService.findOne(functionId, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findOneByIdAndAccountId(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, functionId, accountId, true))
            .thenReturn(Maybe.just(f1Code));

        functionService.findOneByIdAndAccountId(functionId, accountId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("function_id")).isEqualTo(2L);
                assertThat(result.getString("code")).isEqualTo("true");
                testContext.completeNow();
        })));
    }

    @Test
    void findOneByIdAndAccountIdNotExists(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, functionId, accountId, true))
            .thenReturn(Maybe.empty());

        functionService.findOneByIdAndAccountId(functionId, accountId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(functionRepository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(f1Code, f2Code)));

        functionService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("function_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getLong("function_id")).isEqualTo(3L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllAccessibleFunctions(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(functionRepository.findAllAccessibleAndFetch(sessionManager, accountId))
            .thenReturn(Single.just(List.of(f1Code, f2Code)));

        functionService.findAllAccessibleFunctions(accountId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("function_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getLong("function_id")).isEqualTo(3L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(functionRepository.findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of(f1Code, f2Code)));

        functionService.findAllByAccountId(accountId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("function_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getLong("function_id")).isEqualTo(3L);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "code"})
    void saveToAccount(String type, VertxTestContext testContext) {
        Function func = type.equals("file") ? f1File : f1Code;
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Runtime.class, rPython.getRuntimeId()))
            .thenReturn(Maybe.just(rPython));
        when(functionRepository
            .findOneByNameTypeRuntimeAndCreator(sessionManager, func.getName(), 11L, 1L, 1L))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(FunctionType.class, 11L))
            .thenReturn(Maybe.just(ft1));
        when(sessionManager.find(Account.class, accountId))
            .thenReturn(Maybe.just(account));
        when(sessionManager.persist(any(Function.class))).thenReturn(Single.just(func));
        try (MockedStatic<UploadFileHelper> mockedHelper = mockStatic(UploadFileHelper.class);
             MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            if (func.getIsFile()) {
                mockedHelper.when(() -> UploadFileHelper.persistUploadedFile(vertx, f1File.getCode()))
                    .thenReturn(Completable.complete());
                mockedVertx.when(Vertx::currentContext).thenReturn(context);
                when(context.owner()).thenReturn(vertx);
            }
            functionService.saveToAccount(accountId, JsonObject.mapFrom(func),
                testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result.getLong("function_id")).isEqualTo(func.getFunctionId());
                    assertThat(result.getJsonObject("function_type").getLong("artifact_type_id"))
                        .isEqualTo(11L);
                    assertThat(result.getJsonObject("runtime").getLong("runtime_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject("created_by").getLong("account_id")).isEqualTo(1L);
                    testContext.completeNow();
                }))
            );
        }
    }

    @Test
    void saveToAccountAccountNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Runtime.class, rPython.getRuntimeId())).thenReturn(Maybe.just(rPython));
        when(functionRepository
            .findOneByNameTypeRuntimeAndCreator(sessionManager, f1Code.getName(), 11L, 1L, 1L))
            .thenReturn(Maybe.empty());
        // Necessary for stubbing to use doReturn/when instead of when/theReturn
        doReturn(Maybe.just(ft1)).when(sessionManager).find(FunctionType.class, 11L);
        doReturn(Maybe.empty()).when(sessionManager).find(Account.class, accountId);
        functionService.saveToAccount(accountId, JsonObject.mapFrom(f1Code),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountFunctionTypeNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Runtime.class, rPython.getRuntimeId()))
            .thenReturn(Maybe.just(rPython));
        when(functionRepository
            .findOneByNameTypeRuntimeAndCreator(sessionManager, f1Code.getName(), 11L, 1L, 1L))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(FunctionType.class, 11L))
            .thenReturn(Maybe.empty());
        functionService.saveToAccount(accountId, JsonObject.mapFrom(f1Code),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Runtime.class, rPython.getRuntimeId()))
            .thenReturn(Maybe.just(rPython));
        when(functionRepository
            .findOneByNameTypeRuntimeAndCreator(sessionManager, f1Code.getName(), 11L, 1L, 1L))
            .thenReturn(Maybe.just(f1Code));
        when(sessionManager.find(FunctionType.class, 11L))
            .thenReturn(Maybe.empty());
        functionService.saveToAccount(accountId, JsonObject.mapFrom(f1Code),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountNoFileForJava(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Runtime.class, rPython.getRuntimeId())).thenReturn(Maybe.just(rJava));
        when(sessionManager.find(FunctionType.class, 11L)).thenReturn(Maybe.empty());
        functionService.saveToAccount(accountId, JsonObject.mapFrom(f1Code),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage()).isEqualTo("runtime only supports zip archives");
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountRuntimeNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(Runtime.class, rPython.getRuntimeId()))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(FunctionType.class, 11L))
            .thenReturn(Maybe.just(ft1));
        functionService.saveToAccount(accountId, JsonObject.mapFrom(f1Code),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            }))
        );
    }

    @Test
    void updateOwned(VertxTestContext testContext) {
        UpdateFunctionDTO updateFunction = TestDTOProvider.createUpdateFunctionDTO(
            "newcode", false, true, (short) 300, (short) 512);

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, f1Code.getFunctionId(), accountId, false))
            .thenReturn(Maybe.just(f1Code));

        functionService.updateOwned(f1Code.getFunctionId(), accountId, JsonObject.mapFrom(updateFunction),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(f1Code.getCode()).isEqualTo("newcode");
                assertThat(f1Code.getIsPublic()).isEqualTo(true);
                assertThat(f1Code.getTimeoutSeconds()).isEqualTo((short) 300);
                assertThat(f1Code.getMemoryMegabytes()).isEqualTo((short) 512);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "code", "metadata"})
    void updateOwned(String type, VertxTestContext testContext) {
        Function func = type.equals("file") ? f1File : f1Code;
        UpdateFunctionDTO updateFunction = TestDTOProvider.createUpdateFunctionDTO(
            "file-name-id-01", func.getIsFile(), true, (short) 300, (short) 512);
        if (type.equals("metadata")) {
            updateFunction.setCode(null);
        }
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, func.getFunctionId(), accountId, false))
            .thenReturn(Maybe.just(func));
        try (MockedStatic<UploadFileHelper> mockedHelper = mockStatic(UploadFileHelper.class);
             MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            if (func.getIsFile() && updateFunction.getCode() != null) {
                mockedHelper.when(() -> UploadFileHelper.updateFile(vertx, func.getCode(), updateFunction.getCode()))
                    .thenReturn(Completable.complete());
                mockedVertx.when(Vertx::currentContext).thenReturn(context);
                when(context.owner()).thenReturn(vertx);
            }
            functionService.updateOwned(func.getFunctionId(), accountId, JsonObject.mapFrom(updateFunction),
                testContext.succeeding(result -> testContext.verify(() -> {
                    if (updateFunction.getCode() == null) {
                        assertThat(func.getCode()).isEqualTo("true");
                    } else {
                        assertThat(func.getCode()).isEqualTo("file-name-id-01");
                    }
                    assertThat(func.getIsPublic()).isEqualTo(true);
                    assertThat(func.getTimeoutSeconds()).isEqualTo((short) 300);
                    assertThat(func.getMemoryMegabytes()).isEqualTo((short) 512);
                    testContext.completeNow();
                })));
        }
    }

    @Test
    void updateOwnedUpdateNonFileWithFile(VertxTestContext testContext) {
        UpdateFunctionDTO updateFunction = TestDTOProvider.createUpdateFunctionDTO(
            "file-name-id-01", true, true, (short) 300, (short) 512);
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, f1Code.getFunctionId(), accountId, false))
            .thenReturn(Maybe.just(f1Code));
        functionService.updateOwned(f1Code.getFunctionId(), accountId, JsonObject.mapFrom(updateFunction),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage())
                    .isEqualTo("Function can't be updated with zip packaged code");
                testContext.completeNow();
            })));
    }

    @Test
    void updateOwnedUpdateFileWithNonFile(VertxTestContext testContext) {
        UpdateFunctionDTO updateFunction = TestDTOProvider.createUpdateFunctionDTO(
            "def main():\nprint()", false, true, (short) 300, (short) 512);
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, f1File.getFunctionId(), accountId, false))
            .thenReturn(Maybe.just(f1File));
        functionService.updateOwned(f1File.getFunctionId(), accountId, JsonObject.mapFrom(updateFunction),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage())
                    .isEqualTo("Function can only be updated with zip packaged code");
                testContext.completeNow();
            })));
    }

    @Test
    void updateOwnedNotFound(VertxTestContext testContext) {
        UpdateFunctionDTO updateFunction = TestDTOProvider.createUpdateFunctionDTO(
            "def main():\nprint()", false, true, (short) 300, (short) 512);
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, f1Code.getFunctionId(), accountId, false))
            .thenReturn(Maybe.empty());
        functionService.updateOwned(f1Code.getFunctionId(), accountId, JsonObject.mapFrom(updateFunction),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "code"})
    void deleteFromAccount(String type, VertxTestContext testContext) {
        Function func = type.equals("file") ? f1File : f1Code;
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, func.getFunctionId(), accountId, false))
            .thenReturn(Maybe.just(func));
        when(sessionManager.remove(func)).thenReturn(Completable.complete());
        try (MockedStatic<UploadFileHelper> mockedHelper = mockStatic(UploadFileHelper.class);
             MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            if (func.getIsFile()) {
                mockedHelper.when(() -> UploadFileHelper.deleteFile(vertx, func.getCode()))
                    .thenReturn(Completable.complete());
                mockedVertx.when(Vertx::currentContext).thenReturn(context);
                when(context.owner()).thenReturn(vertx);
            }
            functionService.deleteFromAccount(accountId, func.getFunctionId(),
                testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
        }
    }

    @Test
    void deleteFromNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(functionRepository.findByIdAndAccountId(sessionManager, f1Code.getFunctionId(), accountId, false))
            .thenReturn(Maybe.empty());
        functionService.deleteFromAccount(accountId, f1Code.getFunctionId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }
}
