package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PackagePythonCode} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PackagePythonCodeTest {

    private PackagePythonCode packagePythonCode;

    private final Path root = Path.of("src","test", "resources", "uploads");

    @Mock
    private FileSystem fileSystem;

    @Mock
    private File file1, file2;

    @Mock
    private ZipEntry zip;

    @Mock
    private ZipOutputStream zipOutputStream;

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    private Function fCode, fFile;
    private Path rootFolder, sourceCodeInlinePath, sourceCodeFilePath;
    private DeploymentPath deploymentPath;


    @BeforeEach
    void initTest() {
        deploymentPath = new DeploymentPath(1L, config);
        fCode = TestFunctionProvider.createFunction(1L, "code", "def main():\nprint()\n");
        Runtime r1 = TestFunctionProvider.createRuntime(1L);
        fFile = TestFunctionProvider.createFunction(2L, "file", "testfiles.zip", r1, true);
        String fileName = "main.py";
        rootFolder = Path.of("function");
        sourceCodeInlinePath = Path.of(root.toString(), fCode.getFunctionDeploymentId(), fileName);
        sourceCodeFilePath = Path.of(root.toString(), fFile.getFunctionDeploymentId(), fileName);
    }

    @Test
    void zipAllFiles(Vertx vertx, VertxTestContext testContext) {
        packagePythonCode = new PackagePythonCode(vertx, fileSystem, deploymentPath, fCode);

        packagePythonCode.createSourceCode()
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> {
                    assertThat(throwable).isInstanceOf(RuntimeException.class);
                    assertThat(throwable.getMessage()).isEqualTo("runtime only supports zip deployments");
                    testContext.completeNow();
                });

    }
}
