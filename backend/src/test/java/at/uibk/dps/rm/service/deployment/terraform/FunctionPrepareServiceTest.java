package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionPrepareService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionPrepareServiceTest {

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    @BeforeEach
    void initTest() {
        Mockito.lenient().when(vertx.fileSystem()).thenReturn(fileSystem);
    }

    @Test
    void packageCodeLambdaEC2(VertxTestContext testContext) {
        String templatePath = Path.of("faas-templates", "python38", "openfaas").toAbsolutePath().toString();
        String destinationPath = Path.of("build", "deployment_1", "functions", "template", "python38-apollo-rm")
            .toAbsolutePath().toString();

        when(fileSystem.mkdirs(destinationPath)).thenReturn(Completable.complete());
        given(fileSystem.copyRecursive(templatePath, destinationPath, true)).willReturn(Completable.complete());

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

    @Test
    void packageCodeEC2OpenFaas(VertxTestContext testContext) {
        String templatePath = Path.of("faas-templates", "python38", "openfaas").toAbsolutePath().toString();
        String destinationPath = Path.of("build", "deployment_1", "functions", "template", "python38-apollo-rm")
            .toAbsolutePath().toString();

        when(fileSystem.mkdirs(destinationPath)).thenReturn(Completable.complete());
        given(fileSystem.copyRecursive(templatePath, destinationPath, true)).willReturn(Completable.complete());

        FunctionPrepareService service = TestFileServiceProvider.createFunctionFileServiceEC2OpenFaasPython(vertx);
        try (MockedConstruction<PackagePythonCode> ignoredPPC = Mockprovider.mockPackagePythonCode()) {
            service.packageCode()
                .subscribe(result -> testContext.verify(() -> {
                        assertThat(result.getFunctionIdentifiers().size()).isEqualTo(2);
                        assertThat(result.getFunctionIdentifiers().get(0)).isEqualTo("foo1_python38");
                        assertThat(result.getFunctionIdentifiers().get(1)).isEqualTo("foo2_python38");
                        assertThat(result.getDockerFunctionsString()).isEqualTo("  foo1_python38:\n" +
                            "    lang: python38-apollo-rm\n" +
                            "    handler: ./foo1_python38\n" +
                            "    image: user/foo1_python38:latest\n" +
                            "  foo2_python38:\n" +
                            "    lang: python38-apollo-rm\n" +
                            "    handler: ./foo2_python38\n" +
                            "    image: user/foo2_python38:latest\n");
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void packageCodeFunctionTwice(VertxTestContext testContext) {
        String templatePath = Path.of("faas-templates", "python38", "openfaas").toAbsolutePath().toString();
        String destinationPath = Path.of("build", "deployment_1", "functions", "template", "python38-apollo-rm")
            .toAbsolutePath().toString();

        when(fileSystem.mkdirs(destinationPath)).thenReturn(Completable.complete());
        given(fileSystem.copyRecursive(templatePath, destinationPath, true)).willReturn(Completable.complete());

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
    void packageCodeFaasInvalidRuntime(VertxTestContext testContext) {
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
