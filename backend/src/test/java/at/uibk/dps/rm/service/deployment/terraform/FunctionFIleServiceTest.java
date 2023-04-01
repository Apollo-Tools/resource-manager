package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionFIleServiceTest {


    @Test
    void packageCodeFaasVM(Vertx vertx, VertxTestContext testContext) {
        FunctionFileService service = TestFileServiceProvider.createFunctionFileServiceFaasVMPython(vertx);
        Path functionsDir = Paths.get("temp\\test\\functions");
        try (MockedConstruction<PackagePythonCode> ignored = Mockito.mockConstruction(PackagePythonCode.class,
            (mock, context) -> given(mock.composeSourceCode(eq(functionsDir), any(), any()))
                .willReturn(Completable.complete()))) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                        assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python39");
                        assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python39");
                        assertThat(result.getFunctionsString().toString()).isEqualTo("  foo2_python39:\n" +
                            "    lang: python3-flask-debian\n" +
                            "    handler: ./foo2_python39\n" +
                            "    image: user/foo2_python39:latest\n");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void packageCodeVMEdge(Vertx vertx, VertxTestContext testContext) {
        FunctionFileService service = TestFileServiceProvider.createFunctionFileServiceVMEdgePython(vertx);
        Path functionsDir = Paths.get("temp\\test\\functions");
        try (MockedConstruction<PackagePythonCode> ignored = Mockito.mockConstruction(PackagePythonCode.class,
            (mock, context) -> given(mock.composeSourceCode(eq(functionsDir), any(), any()))
                .willReturn(Completable.complete()))) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                        assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python39");
                        assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python39");
                        assertThat(result.getFunctionsString().toString()).isEqualTo("  foo1_python39:\n" +
                            "    lang: python3-flask-debian\n" +
                            "    handler: ./foo1_python39\n" +
                            "    image: user/foo1_python39:latest\n" +
                            "  foo2_python39:\n" +
                            "    lang: python3-flask-debian\n" +
                            "    handler: ./foo2_python39\n" +
                            "    image: user/foo2_python39:latest\n");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void packageCodeFunctionTwice(Vertx vertx, VertxTestContext testContext) {
        FunctionFileService service = TestFileServiceProvider.createFunctionFileServiceFunctionTwicePython(vertx);
        Path functionsDir = Paths.get("temp\\test\\functions");
        try (MockedConstruction<PackagePythonCode> ignored = Mockito.mockConstruction(PackagePythonCode.class,
            (mock, context) -> given(mock.composeSourceCode(eq(functionsDir), any(), any()))
                .willReturn(Completable.complete()))) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(1);
                        assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python39");
                        assertThat(result.getFunctionsString().toString()).isEqualTo("  foo1_python39:\n" +
                            "    lang: python3-flask-debian\n" +
                            "    handler: ./foo1_python39\n" +
                            "    image: user/foo1_python39:latest\n");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void packageCodeNoFunctions(Vertx vertx, VertxTestContext testContext) {
        FunctionFileService service = TestFileServiceProvider.createFunctionFileServiceNoFunctions(vertx);
        Path functionsDir = Paths.get("temp\\test\\functions");
        try (MockedConstruction<PackagePythonCode> ignored = Mockito.mockConstruction(PackagePythonCode.class,
            (mock, context) -> given(mock.composeSourceCode(eq(functionsDir), any(), any()))
                .willReturn(Completable.complete()))) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(0);
                        assertThat(result.getFunctionsString().toString()).isEqualTo("");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void packageCodeFaasInvalidRuntime(Vertx vertx, VertxTestContext testContext) {
        FunctionFileService service = TestFileServiceProvider.createFunctionFileServiceVMEdgeInvalidRuntime(vertx);
        Path functionsDir = Paths.get("temp\\test\\functions");
        try (MockedConstruction<PackagePythonCode> ignored = Mockito.mockConstruction(PackagePythonCode.class,
            (mock, context) -> given(mock.composeSourceCode(eq(functionsDir), any(), any()))
                .willReturn(Completable.complete()))) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeNotSupportedException.class);
                        testContext.completeNow();
                    })
                );
        }
    }

}
