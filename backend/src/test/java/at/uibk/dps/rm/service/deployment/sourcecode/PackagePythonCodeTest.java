package at.uibk.dps.rm.service.deployment.sourcecode;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private FileSystem fileSystem;

    @Mock
    private Path srcPath;

    @Mock
    private File srcFile;

    @Mock
    private ZipEntry zipEntry;

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    private Function function;
    private Path rootDir, layersDir;

    @BeforeEach
    void initTest(Vertx vertx) {
        DeploymentPath deploymentPath = new DeploymentPath(1L, config);
        function = TestFunctionProvider.createFunction(1L, "code", "def main():\nprint()\n");
        packagePythonCode = new PackagePythonCode(vertx, fileSystem, deploymentPath, function);
        rootDir = Paths.get("path", "to", "root");
        layersDir = Paths.get(deploymentPath.getLayersFolder().toString(), function.getFunctionDeploymentId());
    }

    @Test
    void zipAllFilesFiles() {
        byte[] bytes = "hello".getBytes();
        byte[] expectedBytes = new byte[1024];
        System.arraycopy(bytes, 0, expectedBytes, 0, bytes.length);

        when(srcPath.getParent()).thenReturn(srcPath);
        when(srcPath.toFile()).thenReturn(srcFile);
        when(srcFile.listFiles()).thenReturn(new File[]{srcFile});
        when(srcFile.isHidden()).thenReturn(false);
        when(srcFile.isDirectory()).thenReturn(false);
        when(srcFile.getName()).thenReturn("main.py");

        try(MockedConstruction<FileOutputStream> ignoreFos = Mockprovider.mockFileOutputStream(rootDir + "/" +
            function.getFunctionDeploymentId() + ".zip");
                MockedConstruction<ZipOutputStream> ignoreZos = Mockprovider.mockZipOutputStream(expectedBytes,
                    bytes.length);
                MockedConstruction<FileInputStream> ignoreFis = Mockprovider.mockFileInputStream(bytes, srcFile,
                    Path.of("faas-templates", "python38", "lambda", "lambda.py").toFile())) {
            packagePythonCode.zipAllFiles(rootDir, srcPath, function.getFunctionDeploymentId());
        }
    }

    @Test
    void zipAllFilesSourceFilesNotFound() {
        when(srcPath.getParent()).thenReturn(srcPath);
        when(srcPath.toFile()).thenReturn(srcFile);
        when(srcFile.listFiles()).thenReturn(null);

        try(MockedConstruction<FileOutputStream> ignoreFos = Mockprovider.mockFileOutputStream(rootDir + "/" +
            function.getFunctionDeploymentId() + ".zip");
                MockedConstruction<ZipOutputStream> ignoreZos = Mockprovider.mockZipOutputStream()) {
            assertThrows(DeploymentTerminationFailedException.class, () -> packagePythonCode.zipAllFiles(rootDir,
                srcPath, function.getFunctionDeploymentId()));
        }
    }

    @Test
    void zipAllFilesPathNotFound() {
        assertThrows(RuntimeException.class, () -> packagePythonCode.zipAllFiles(rootDir, srcPath,
            function.getFunctionDeploymentId()));
    }

    @Test
    void getUnzippedFilesDestNoLayer() throws IOException {
        File destDir = Paths.get("src", "test", "resources", "upload").toFile();

        when(zipEntry.getName()).thenReturn("main.py");
        when(fileSystem.mkdirsBlocking(layersDir.toString())).thenReturn(fileSystem);

        List<File> result = packagePythonCode.getUnzippedFilesDest(zipEntry, destDir);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(new File(destDir, "main.py"));
    }

    @Test
    void getUnzippedFilesDestLayer() throws IOException {
        File destDir = Paths.get("src", "test", "resources", "upload").toFile();

        when(zipEntry.getName()).thenReturn("requirements.txt");
        when(zipEntry.getSize()).thenReturn(2L);
        when(fileSystem.mkdirsBlocking(layersDir.toString())).thenReturn(fileSystem);

        List<File> result = packagePythonCode.getUnzippedFilesDest(zipEntry, destDir);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(new File(destDir, "requirements.txt"));
        assertThat(result.get(1)).isEqualTo(new File(layersDir.toString(), "requirements.txt"));
    }

    @Test
    void getUnzippedFilesEmptyRequirements() throws IOException {
        File destDir = Paths.get("src", "test", "resources", "upload").toFile();

        when(zipEntry.getName()).thenReturn("requirements.txt");
        when(zipEntry.getSize()).thenReturn(0L);
        when(fileSystem.mkdirsBlocking(layersDir.toString())).thenReturn(fileSystem);

        List<File> result = packagePythonCode.getUnzippedFilesDest(zipEntry, destDir);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(new File(destDir, "requirements.txt"));
    }
}
