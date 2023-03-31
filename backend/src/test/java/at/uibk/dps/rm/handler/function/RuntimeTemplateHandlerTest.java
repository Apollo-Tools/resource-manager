package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
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
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RuntimeTemplateHandlerTest {

    private RuntimeTemplateHandler runtimeTemplateHandler;

    @Mock
    RuntimeChecker runtimeChecker;

    @Mock
    FileSystemChecker fileSystemChecker;

    @Mock
    RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        runtimeTemplateHandler = new RuntimeTemplateHandler(runtimeChecker, fileSystemChecker);
    }

    @Test
    void getOneExists(VertxTestContext testContext) {
        long runtimeId = 1L;
        String templatePath = "path/to/template";
        String templateContent = "def main():\n\treturn -1";
        JsonObject runtime = JsonObject.mapFrom(TestFunctionProvider.createRuntime(1L, "python3.9",
            templatePath));

        when(rc.pathParam("id")).thenReturn(String.valueOf(runtimeId));
        when(runtimeChecker.checkFindOne(runtimeId)).thenReturn(Single.just(runtime));
        when(fileSystemChecker.checkTemplatePathExists(templatePath)).thenReturn(Completable.complete());
        when(fileSystemChecker.checkGetFileTemplate(templatePath))
            .thenReturn(Single.just(templateContent));

        runtimeTemplateHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.fieldNames().size()).isEqualTo(1);
                assertThat(result.getString("template")).isEqualTo(templateContent);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long runtimeId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(runtimeId));
        when(runtimeChecker.checkFindOne(runtimeId)).thenReturn(Single.error(NotFoundException::new));

        runtimeTemplateHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getOneTemplatePathNotExists(VertxTestContext testContext) {
        long runtimeId = 1L;
        String templatePath = "path/to/template";
        JsonObject runtime = JsonObject.mapFrom(TestFunctionProvider.createRuntime(1L, "python3.9",
            templatePath));

        when(rc.pathParam("id")).thenReturn(String.valueOf(runtimeId));
        when(runtimeChecker.checkFindOne(runtimeId)).thenReturn(Single.just(runtime));
        when(fileSystemChecker.checkTemplatePathExists(templatePath))
            .thenReturn(Completable.error(NotFoundException::new));

        runtimeTemplateHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void getOneTemplateIsBlank(VertxTestContext testContext) {
        long runtimeId = 1L;
        String templatePath = "path/to/template";
        JsonObject runtime = JsonObject.mapFrom(TestFunctionProvider.createRuntime(1L, "python3.9",
            templatePath));

        when(rc.pathParam("id")).thenReturn(String.valueOf(runtimeId));
        when(runtimeChecker.checkFindOne(runtimeId)).thenReturn(Single.just(runtime));
        when(fileSystemChecker.checkTemplatePathExists(templatePath)).thenReturn(Completable.complete());
        when(fileSystemChecker.checkGetFileTemplate(templatePath))
            .thenReturn(Single.error(NotFoundException::new));

        runtimeTemplateHandler.getOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
