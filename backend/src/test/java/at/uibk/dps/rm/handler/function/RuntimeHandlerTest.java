package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RuntimeHandlerTest {

    private RuntimeHandler runtimeHandler;

    @Mock
    private RuntimeChecker runtimeChecker;

    @Mock
    private FileSystemChecker fileSystemChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        runtimeHandler = new RuntimeHandler(runtimeChecker, fileSystemChecker);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath +
            "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeChecker.checkForDuplicateEntity(jsonObject)).thenReturn(Completable.complete());
        when(fileSystemChecker.checkTemplatePathExists(templatePath)).thenReturn(Completable.complete());
        when(runtimeChecker.submitCreate(jsonObject)).thenReturn(Single.just(jsonObject));

        runtimeHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getString("name")).isEqualTo(name);
                    assertThat(result.getString("template_path")).isEqualTo(templatePath);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postTemplateNotFound(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/doesnotexist.py";
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath +
            "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeChecker.checkForDuplicateEntity(jsonObject)).thenReturn(Completable.complete());
        when(fileSystemChecker.checkTemplatePathExists(templatePath))
            .thenReturn(Completable.error(NotFoundException::new));

        runtimeHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneAlreadyExists(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath +
            "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeChecker.checkForDuplicateEntity(jsonObject))
            .thenReturn(Completable.error(AlreadyExistsException::new));
        when(fileSystemChecker.checkTemplatePathExists(templatePath)).thenReturn(Completable.complete());

        runtimeHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneValid(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject runtime = JsonObject.mapFrom(TestFunctionProvider.createRuntime(entityId));
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"template_path\":  \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(runtimeChecker.checkFindOne(entityId)).thenReturn(Single.just(runtime));
        when(fileSystemChecker.checkTemplatePathExists(templatePath)).thenReturn(Completable.complete());
        when(runtimeChecker.submitUpdate(jsonObject, runtime)).thenReturn(Completable.complete());

        runtimeHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"template_path\":  \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(runtimeChecker.checkFindOne(entityId)).thenReturn(Single.error(NotFoundException::new));

        runtimeHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateFilePathNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        JsonObject runtime = JsonObject.mapFrom(TestFunctionProvider.createRuntime(entityId));
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"template_path\":  \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(runtimeChecker.checkFindOne(entityId)).thenReturn(Single.just(runtime));
        when(fileSystemChecker.checkTemplatePathExists(templatePath))
            .thenReturn(Completable.error(NotFoundException::new));

        runtimeHandler.updateOne(rc)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkTemplatePathExists(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath +
            "\"}");

        when(fileSystemChecker.checkTemplatePathExists(templatePath)).thenReturn(Completable.complete());

        runtimeHandler.checkTemplatePathExists(requestBody)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkTemplatePathNotExists(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/doesnotexist.py";
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath +
            "\"}");

        when(fileSystemChecker.checkTemplatePathExists(templatePath))
            .thenReturn(Completable.error(NotFoundException::new));

        runtimeHandler.checkTemplatePathExists(requestBody)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkTemplatePathNotinRequestBody(VertxTestContext testContext) {
        String name = "python";
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\"}");

        runtimeHandler.checkTemplatePathExists(requestBody)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }
}
