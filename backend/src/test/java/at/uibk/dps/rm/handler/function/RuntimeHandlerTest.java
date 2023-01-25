package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RuntimeHandlerTest {

    private RuntimeHandler runtimeHandler;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private FilePathService filePathService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        runtimeHandler = new RuntimeHandler(runtimeService, filePathService);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeService.existsOneByName(name)).thenReturn(Single.just(false));
        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(true));
        when(runtimeService.save(jsonObject)).thenReturn(Single.just(jsonObject));

        runtimeHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getString("name")).isEqualTo(name);
                    assertThat(result.getString("template_path")).isEqualTo(templatePath);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postTemplateNotFound(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/doesnotexist.py";
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeService.existsOneByName(name)).thenReturn(Single.just(false));
        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(false));

        runtimeHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
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
        JsonObject jsonObject = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(runtimeService.existsOneByName(name)).thenReturn(Single.just(true));
        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(true));

        runtimeHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void updateOneValid(VertxTestContext testContext) {
        long entityId = 1L;
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"template_path\":  \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(runtimeService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(true));
        when(runtimeService.update(jsonObject)).thenReturn(Completable.complete());

        runtimeHandler.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(runtimeService).findOne(entityId);
        verify(filePathService).templatePathExists(templatePath);
        verify(runtimeService).update(jsonObject);
        testContext.completeNow();
    }

    @Test
    void updateOneEntityNotFound(VertxTestContext testContext) {
        long entityId = 1L;
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"template_path\":  \"" + templatePath + "\"}");
        Single<JsonObject> handler = new SingleHelper<JsonObject>().getEmptySingle();

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(runtimeService.findOne(entityId)).thenReturn(handler);

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
        String templatePath = "./filepathtest/filepathtest.py";
        JsonObject jsonObject = new JsonObject("{\"template_path\":  \"" + templatePath + "\"}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(runtimeService.findOne(entityId)).thenReturn(Single.just(jsonObject));
        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(false));

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
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath + "\"}");

        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(true));

        runtimeHandler.checkTemplatePathExists(requestBody)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(filePathService).templatePathExists(templatePath);
        testContext.completeNow();
    }

    @Test
    void checkTemplatePathNotExists(VertxTestContext testContext) {
        String name = "python";
        String templatePath = "./filepathtest/doesnotexist.py";
        JsonObject requestBody = new JsonObject("{\"name\": \"" + name + "\", \"template_path\": \"" + templatePath + "\"}");

        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(false));

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
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
        testContext.completeNow();
    }
}
