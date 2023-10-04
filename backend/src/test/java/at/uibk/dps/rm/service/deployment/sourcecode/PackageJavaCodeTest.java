package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PackagePythonCode} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PackageJavaCodeTest {

    private PackageJavaCode packageJavaCode;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private DeploymentPath dp1;

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    private Function fInline, fFile;
    private Path rootDir;

    @BeforeEach
    void initTest() {
        fInline = TestFunctionProvider.createFunction(1L, "code", "def main():\nprint()\n");
        Runtime r1 = TestFunctionProvider.createRuntime(1L);
        fFile = TestFunctionProvider.createFunction(2L, "file", "testfiles_java.zip", r1,
            true);
        rootDir = Path.of("src","test", "resources", "uploads");

        when(dp1.getFunctionsFolder()).thenReturn(rootDir);
    }

    @Test
    void createSourceCodeFile(Vertx vertx, VertxTestContext testContext) {
        if (vertx.fileSystem().existsBlocking(rootDir.toString())) {
            vertx.fileSystem().deleteRecursiveBlocking(rootDir.toString(), true);
        }
        vertx.fileSystem().mkdirsBlocking(rootDir.toString());
        packageJavaCode = new PackageJavaCode(vertx, fileSystem, dp1, fFile);
        config.setUploadPersistDirectory(Paths.get("src","test","resources", "testfiles").toFile()
            .getAbsolutePath());
        Path uploadDir = Path.of(rootDir.toString(), fFile.getFunctionDeploymentId());

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config)) {
            packageJavaCode.createSourceCode()
                .subscribe(() -> testContext.verify(() -> {
                        Path[] paths = {Path.of(uploadDir.toString(), "build.gradle"),
                            Path.of(uploadDir.toString(), "settings.gradle"),
                            Path.of(uploadDir.toString(), "entrypoint", "build.gradle"),
                            Path.of(uploadDir.toString(), "entrypoint", "settings.gradle"),
                            Path.of(uploadDir.toString(), "entrypoint", "src", "main", "java", "org", "apollorm",
                                "entrypoint", "App.java"),
                            Path.of(uploadDir.toString(), "entrypoint", "src", "main", "java", "org", "apollorm",
                                "entrypoint", "Request.java"),
                            Path.of(uploadDir.toString(), "entrypoint", "src", "main", "java", "org", "apollorm",
                                "entrypoint", "RequestDeserializer.java"),
                            Path.of(uploadDir.toString(), "function", "build.gradle"),
                            Path.of(uploadDir.toString(), "function", "settings.gradle"),
                            Path.of(uploadDir.toString(), "function", "src", "main", "java", "org", "apollorm",
                                "function", "File.java"),
                            Path.of(uploadDir.toString(), "function", "src", "main", "java", "org", "apollorm",
                                "function", "Main.java"),
                            Path.of(uploadDir.toString(), "function", "src", "main", "java", "org", "apollorm",
                                "function", "Result.java"),
                            Path.of(uploadDir.toString(), "model", "build.gradle"),
                            Path.of(uploadDir.toString(), "model", "settings.gradle"),
                            Path.of(uploadDir.toString(), "model", "src", "main", "java", "org", "apollorm",
                                "model", "FunctionHandler.java"),
                            Path.of(uploadDir.toString(), "model", "src", "main", "java", "org", "apollorm",
                                "model", "exception", "AWSErrorResponse.java"),
                            Path.of(uploadDir.toString(), "model", "src", "main", "java", "org", "apollorm",
                                "model", "exception", "FunctionException.java"),
                        };
                        for (Path path : paths) {
                            assertThat(vertx.fileSystem()
                                .existsBlocking(path.toString()))
                                .isEqualTo(true);
                            assertThat(vertx.fileSystem().readFileBlocking(path.toString()).length())
                                .isGreaterThan(0);
                        }
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }
    }

    @Test
    void createSourceCodeInline(Vertx vertx, VertxTestContext testContext) {
        packageJavaCode = new PackageJavaCode(vertx, fileSystem, dp1, fInline);
        config.setUploadPersistDirectory(Paths.get("src","test","resources", "testfiles").toFile()
            .getAbsolutePath());

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config)) {
            packageJavaCode.createSourceCode()
                .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                    throwable -> testContext.verify(() -> {
                        assertThat(throwable).isInstanceOf(RuntimeException.class);
                        assertThat(throwable.getMessage()).isEqualTo("runtime only supports zip deployments");
                        testContext.completeNow();
                    })
                );
        }
    }
}
