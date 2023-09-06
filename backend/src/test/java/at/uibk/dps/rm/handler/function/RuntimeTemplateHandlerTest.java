package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link RuntimeTemplateHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RuntimeTemplateHandlerTest {



    private RuntimeTemplateHandler runtimeTemplateHandler;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private RoutingContext rc;

    @Mock
    private Context context;

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        runtimeTemplateHandler = new RuntimeTemplateHandler(runtimeService);
    }

    @Test
    void getOneExists(VertxTestContext testContext) {
        long runtimeId = 1L;
        String templatePath = "path/to/template";
        String templateContent = "def main():\n\treturn -1";
        JsonObject runtime = JsonObject.mapFrom(TestFunctionProvider.createRuntime(1L, "python3.9",
            templatePath));

        when(rc.pathParam("id")).thenReturn(String.valueOf(runtimeId));
        when(runtimeService.findOne(runtimeId)).thenReturn(Single.just(runtime));

        try (MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            when(context.owner()).thenReturn(vertx);
            when(vertx.fileSystem()).thenReturn(fileSystem);
            when(fileSystem.exists(templatePath)).thenReturn(Single.just(true));
            when(fileSystem.readFile(templatePath)).thenReturn(Single.just(Buffer.buffer(templateContent)));
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            runtimeTemplateHandler.getOne(rc)
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.fieldNames().size()).isEqualTo(1);
                        assertThat(result.getString("template")).isEqualTo(templateContent);
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void getOneNotFound(VertxTestContext testContext) {
        long runtimeId = 1L;

        when(rc.pathParam("id")).thenReturn(String.valueOf(runtimeId));
        when(runtimeService.findOne(runtimeId)).thenReturn(Single.error(NotFoundException::new));

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
        when(runtimeService.findOne(runtimeId)).thenReturn(Single.just(runtime));
        try (MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            when(context.owner()).thenReturn(vertx);
            when(vertx.fileSystem()).thenReturn(fileSystem);
            when(fileSystem.exists(templatePath)).thenReturn(Single.just(false));
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            runtimeTemplateHandler.getOne(rc)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                        testContext.completeNow();
                    })
                );
        }
    }

    @Test
    void getOneTemplateIsBlank(VertxTestContext testContext) {
        long runtimeId = 1L;
        String templatePath = "";
        JsonObject runtime = JsonObject.mapFrom(TestFunctionProvider.createRuntime(1L, "python3.9",
            templatePath));

        when(rc.pathParam("id")).thenReturn(String.valueOf(runtimeId));
        when(runtimeService.findOne(runtimeId)).thenReturn(Single.just(runtime));

        try (MockedStatic<Vertx> mockedVertx = Mockprovider.mockVertx()) {
            when(context.owner()).thenReturn(vertx);
            when(vertx.fileSystem()).thenReturn(fileSystem);
            when(fileSystem.exists(templatePath)).thenReturn(Single.just(true));
            mockedVertx.when(Vertx::currentContext).thenReturn(context);
            runtimeTemplateHandler.getOne(rc)
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                        testContext.completeNow();
                    })
                );
        }
    }
}
