package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PackageSourceCode} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PackageSourceCodeTest {

    /**
     * Implements a concrete class of the {@link PackageSourceCode} class.
     */
    static class ConcretePackageSourceCode extends PackageSourceCode {

        /**
         * Create an instance from vertx and the fileSystem.
         *
         * @param vertx the vertx instance
         * @param fileSystem the vertx file system
         */
        protected ConcretePackageSourceCode(Vertx vertx, FileSystem fileSystem, Path root, Function function) {
            super(vertx, fileSystem, root, function,
                "main.py");
            if (vertx.fileSystem().existsBlocking(root.toString())) {
                vertx.fileSystem().deleteRecursiveBlocking(root.toString(), true);
            }
            vertx.fileSystem().mkdirsBlocking(root.toString());
        }

        @Override
        protected void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier) {
            // necessary override can be empty for tests
        }
    }

    private ConcretePackageSourceCode packageSourceCode;

    private final Path root = Path.of("src","test", "resources", "uploads");

    @Mock
    private FileSystem fileSystem;

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    private Function fCode, fFile;
    Path rootFolder, sourceCodeInlinePath, sourceCodeFilePath;

    @BeforeEach
    void initTest() {
        fCode = TestFunctionProvider.createFunction(1L, "code", "def main():\nprint()\n");
        Runtime r1 = TestFunctionProvider.createRuntime(1L);
        fFile = TestFunctionProvider.createFunction(2L, "file", "testfiles.zip", r1, true);
        String fileName = "main.py";
        rootFolder = Path.of("function");
        sourceCodeInlinePath = Path.of(root.toString(), fCode.getFunctionDeploymentId(), fileName);
        sourceCodeFilePath = Path.of(root.toString(), fFile.getFunctionDeploymentId(), fileName);
    }

    @Test
    void composeSourceCode(Vertx vertx, VertxTestContext testContext) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fCode);

        when(fileSystem.mkdirs(sourceCodeInlinePath.getParent().toString()))
            .thenReturn(Completable.complete());
        when(fileSystem.writeFile(sourceCodeInlinePath.toString(), Buffer.buffer(fCode.getCode())))
            .thenReturn(Completable.complete());

        packageSourceCode.composeSourceCode()
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void composeSourceCodeWriteFileFailed(Vertx vertx, VertxTestContext testContext) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fCode);

        when(fileSystem.mkdirs(sourceCodeInlinePath.getParent().toString()))
            .thenReturn(Completable.complete());
        when(fileSystem.writeFile(sourceCodeInlinePath.toString(), Buffer.buffer(fCode.getCode())))
            .thenReturn(Completable.error(IOException::new));

        packageSourceCode.composeSourceCode()
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IOException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void composeSourceCodeMkdirsFailed(Vertx vertx, VertxTestContext testContext) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fCode);

        when(fileSystem.mkdirs(sourceCodeInlinePath.getParent().toString()))
            .thenReturn(Completable.error(IOException::new));

        packageSourceCode.composeSourceCode()
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IOException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void createSourceCodeFile(Vertx vertx, VertxTestContext testContext) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        config.setUploadPersistDirectory(Paths.get("src","test","resources", "testfiles").toFile()
            .getAbsolutePath());
        Path uploadDir = Path.of(root.toString(), fFile.getFunctionDeploymentId());

        try (MockedConstruction<ConfigUtility> ignoredConfig = Mockprovider.mockConfig(config)) {
            packageSourceCode.createSourceCode()
                .subscribe(() -> testContext.verify(() -> {
                        String mainPath = Path.of(uploadDir.toString(), "main.py").toString();
                        String requirementsPath = Path.of(uploadDir.toString(), "requirements.txt").toString();
                        assertThat(vertx.fileSystem()
                            .existsBlocking(mainPath))
                            .isEqualTo(true);
                        assertThat(vertx.fileSystem()
                            .existsBlocking(requirementsPath))
                            .isEqualTo(true);
                        assertThat(vertx.fileSystem()
                            .readFileBlocking(mainPath)
                            .getString(0, 24))
                            .isEqualTo("def main():\n    print()\n");
                        assertThat(vertx.fileSystem()
                            .readFileBlocking(requirementsPath)
                            .length())
                            .isEqualTo(0);
                        testContext.completeNow();
                    }),
                    throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
        }

    }
}
