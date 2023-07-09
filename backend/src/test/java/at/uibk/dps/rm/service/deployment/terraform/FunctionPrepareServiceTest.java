package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Implements tests for the {@link FunctionPrepareService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionPrepareServiceTest {


    @Disabled
    @Test
    void packageCodeLambdaEC2(Vertx vertx, VertxTestContext testContext) {
        FunctionPrepareService service = TestFileServiceProvider.createFunctionFileServiceLambdaEc2Python(vertx);
        try (MockedConstruction<PackagePythonCode> ignored = Mockprovider.mockPackagePythonCode()) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                        assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python38");
                        assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python38");
                        assertThat(result.getDockerFunctionsString()).isEqualTo("  foo2_python38:\n" +
                            "    lang: python38-apollo-rm\n" +
                            "    handler: ./foo2_python38\n" +
                            "    image: user/foo2_python38:latest\n");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Disabled
    @Test
    void packageCodeEC2OpenFaas(Vertx vertx, VertxTestContext testContext) {
        FunctionPrepareService service = TestFileServiceProvider.createFunctionFileServiceEC2OpenFaasPython(vertx);
        try (MockedConstruction<PackagePythonCode> ignored = Mockprovider.mockPackagePythonCode()) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                        assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python38");
                        assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python38");
                        assertThat(result.getDockerFunctionsString()).isEqualTo("  foo1_python38:\n" +
                            "    lang: python38-apollo-rm\n" +
                            "    handler: ./foo1_python38\n" +
                            "    image: user/foo1_python38:latest\n" +
                            "  foo2_python39:\n" +
                            "    lang: python38-apollo-rm\n" +
                            "    handler: ./foo2_python38\n" +
                            "    image: user/foo2_python38:latest\n");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Disabled
    @Test
    void packageCodeFunctionTwice(Vertx vertx, VertxTestContext testContext) {
        FunctionPrepareService service = TestFileServiceProvider.createFunctionFileServiceFunctionTwicePython(vertx);
        try (MockedConstruction<PackagePythonCode> ignored = Mockprovider.mockPackagePythonCode()) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(1);
                        assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python38");
                        assertThat(result.getDockerFunctionsString()).isEqualTo("  foo1_python38:\n" +
                            "    lang: python38-apollo-rm\n" +
                            "    handler: ./foo1_python38\n" +
                            "    image: user/foo1_python38:latest\n");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void packageCodeNoFunctions(Vertx vertx, VertxTestContext testContext) {
        FunctionPrepareService service = TestFileServiceProvider.createFunctionFileServiceNoFunctions(vertx);
        try (MockedConstruction<PackagePythonCode> ignored = Mockprovider.mockPackagePythonCode()) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(0);
                        assertThat(result.getDockerFunctionsString()).isEqualTo("");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void packageCodeFaasInvalidRuntime(Vertx vertx, VertxTestContext testContext) {
        FunctionPrepareService service = TestFileServiceProvider.createFunctionFileServiceEC2OpenFaasInvalidRuntime(vertx);
        try (MockedConstruction<PackagePythonCode> ignored = Mockprovider.mockPackagePythonCode()) {
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