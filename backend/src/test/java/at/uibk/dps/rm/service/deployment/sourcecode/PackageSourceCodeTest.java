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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    @Mock
    private File file1, file2;

    @Mock
    private ZipEntry zip;

    @Mock
    private ZipOutputStream zipOutputStream;

    private final ConfigDTO config = TestConfigProvider.getConfigDTO();

    private Function fCode, fFile;
    private Path sourceCodeInlinePath;

    @BeforeEach
    void initTest() {
        fCode = TestFunctionProvider.createFunction(1L, "code", "def main():\nprint()\n");
        Runtime r1 = TestFunctionProvider.createRuntime(1L);
        fFile = TestFunctionProvider.createFunction(2L, "file", "testfiles.zip", r1, true);
        String fileName = "main.py";
        sourceCodeInlinePath = Path.of(root.toString(), fCode.getFunctionDeploymentId(), fileName);
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
                        String requirementsPath = Path.of(uploadDir.toString(), "deps", "requirements.txt")
                            .toString();
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

    @Test
    void zipFileSingleFile(Vertx vertx) throws IOException {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        byte[] bytes = "hello".getBytes();
        byte[] expectedBytes = new byte[1024];
        System.arraycopy(bytes, 0, expectedBytes, 0, bytes.length);

        when(file1.isHidden()).thenReturn(false);
        when(file1.isDirectory()).thenReturn(false);
        doNothing().when(zipOutputStream).write(expectedBytes, 0, bytes.length);

        try (MockedConstruction<FileInputStream> ignore = Mockprovider.mockFileInputStream(file1, bytes)) {
            packageSourceCode.zipFile(file1, "file1", zipOutputStream);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"dirfile1/", "dirfile1"})
    void zipFileDirectory(String file1Name, Vertx vertx) throws IOException {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        byte[] bytes = "hello".getBytes();
        byte[] expectedBytes = new byte[1024];
        System.arraycopy(bytes, 0, expectedBytes, 0, bytes.length);
        String file2Name = "file2";

        when(file1.isHidden()).thenReturn(false);
        when(file1.isDirectory()).thenReturn(true);
        when(file1.getName()).thenReturn(file1Name);
        when(file1.listFiles()).thenReturn(new File[]{file2});
        when(file2.getName()).thenReturn(file2Name);
        doNothing().when(zipOutputStream).write(expectedBytes, 0, bytes.length);

        try (MockedConstruction<FileInputStream> ignore = Mockprovider.mockFileInputStream(file2, bytes)) {
            packageSourceCode.zipFile(file1, "file1", zipOutputStream);
            verify(zipOutputStream).putNextEntry(argThat((ZipEntry entry) ->
                entry.getName().equals("file1" + (file1Name.endsWith("/") ? "" : "/"))));
            verify(zipOutputStream).putNextEntry(argThat((ZipEntry entry) ->
                entry.getName().equals("file1/file2")));
        }
    }

    @Test
    void zipFileHiddenFile(Vertx vertx) throws IOException {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        when(file1.isHidden()).thenReturn(true);

        packageSourceCode.zipFile(file1, "file1", new ZipOutputStream(OutputStream.nullOutputStream()));
    }

    @Test
    void unzipAllFilesNotExists(Vertx vertx) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        config.setUploadPersistDirectory(Paths.get("src","test","resources", "testfiles").toFile()
            .getAbsolutePath());
        Path path = Paths.get("src", "test", "resources", "not", "existing");

        assertThrows(RuntimeException.class, () -> packageSourceCode.unzipAllFiles(path));
    }

    @Test
    void readAndSaveUnzippedFileDirectoryExists(Vertx vertx) throws IOException {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        List<File> newFiles = List.of(file1);

        when(zip.isDirectory()).thenReturn(true);
        when(file1.mkdirs()).thenReturn(false);
        when(file1.exists()).thenReturn(true);

        packageSourceCode.readAndSaveUnzippedFile(zip, newFiles, new ZipInputStream(InputStream.nullInputStream()));
    }

    @Test
    void readAndSaveUnzippedFileDirectoryFailed(Vertx vertx) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        List<File> newFiles = List.of(file1);

        when(file1.toString()).thenReturn("directory-file");
        when(zip.isDirectory()).thenReturn(true);
        when(file1.mkdirs()).thenReturn(false);
        when(file1.exists()).thenReturn(false);

        IOException throwable = assertThrows(IOException.class, () -> packageSourceCode.readAndSaveUnzippedFile(zip,
            newFiles, new ZipInputStream(InputStream.nullInputStream())));
        assertThat(throwable.getMessage()).isEqualTo("Failed to create directory directory-file");
    }

    @Test
    void readAndSaveUnzippedFileParentExists(Vertx vertx) throws IOException {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        List<File> newFiles = List.of(file1);

        when(zip.isDirectory()).thenReturn(false);
        when(file1.getParentFile()).thenReturn(file2);
        when(file2.isDirectory()).thenReturn(false);
        when(file2.mkdirs()).thenReturn(false);
        when(file2.exists()).thenReturn(true);

        try (MockedConstruction<FileOutputStream> ignore = Mockprovider.mockFileOutputStream(file1)) {
            packageSourceCode.readAndSaveUnzippedFile(zip, newFiles, new ZipInputStream(InputStream.nullInputStream()));
        }
    }

    @Test
    void readAndSaveUnzippedFileParentFailed(Vertx vertx) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem, root, fFile);
        List<File> newFiles = List.of(file1);

        when(file2.toString()).thenReturn("parent-file");
        when(zip.isDirectory()).thenReturn(false);
        when(file1.getParentFile()).thenReturn(file2);
        when(file2.isDirectory()).thenReturn(false);
        when(file2.mkdirs()).thenReturn(false);
        when(file2.exists()).thenReturn(false);

        IOException throwable = assertThrows(IOException.class, () -> packageSourceCode.readAndSaveUnzippedFile(zip,
            newFiles, new ZipInputStream(InputStream.nullInputStream())));
        assertThat(throwable.getMessage()).isEqualTo("Failed to create directory parent-file");
    }

    @Test
    void newFileZipSlip() {
        String zipFilePath = Paths.get("..", "..", "..", "..", "file1").toString();
        File destDir = Paths.get("src", "test", "resources", "uploads").toFile();

        when(zip.getName()).thenReturn(zipFilePath);

        IOException throwable = assertThrows(IOException.class, () -> PackageSourceCode.newFile(destDir, zip));
        assertThat(throwable.getMessage()).isEqualTo("Entry is outside of the target dir: " + zipFilePath);
    }
}
