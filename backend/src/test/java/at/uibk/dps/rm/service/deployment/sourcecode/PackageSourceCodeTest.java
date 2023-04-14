package at.uibk.dps.rm.service.deployment.sourcecode;

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

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PackageSourceCodeTest {

    static class ConcretePackageSourceCode extends PackageSourceCode {
        protected ConcretePackageSourceCode(Vertx vertx, FileSystem fileSystem) {
            super(vertx, fileSystem);
        }

        @Override
        protected void zipAllFiles(Path rootFolder, Path sourceCode, String functionIdentifier) {
            // necessary override can be empty for tests
        }
    }

    private ConcretePackageSourceCode packageSourceCode;

    @Mock
    private FileSystem fileSystem;

    @BeforeEach
    void initTest(Vertx vertx) {
        packageSourceCode = new ConcretePackageSourceCode(vertx, fileSystem);
    }

    @Test
    void composeSourceCode(VertxTestContext testContext) {
        Path rootFolder = Path.of("./rootFolder");
        String functionIdentifier = "funct";
        String code = "def main():\n\treturn 0\n";
        String fileName = "cloud_function.txt";
        Path sourceCodePath = Path.of(rootFolder.toString(), functionIdentifier, fileName);

        when(fileSystem.mkdirs(sourceCodePath.getParent().toString()))
            .thenReturn(Completable.complete());
        when(fileSystem.writeFile(sourceCodePath.toString(), Buffer.buffer(code))).thenReturn(Completable.complete());

        packageSourceCode.composeSourceCode(rootFolder, functionIdentifier, code)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void composeSourceCodeWriteFileFailed(VertxTestContext testContext) {
        Path rootFolder = Path.of("./rootFolder");
        String functionIdentifier = "funct";
        String code = "def main():\n\treturn 0\n";
        String fileName = "cloud_function.txt";
        Path sourceCodePath = Path.of(rootFolder.toString(), functionIdentifier, fileName);

        when(fileSystem.mkdirs(sourceCodePath.getParent().toString()))
            .thenReturn(Completable.complete());
        when(fileSystem.writeFile(sourceCodePath.toString(), Buffer.buffer(code)))
            .thenReturn(Completable.error(IOException::new));

        packageSourceCode.composeSourceCode(rootFolder, functionIdentifier, code)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IOException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void composeSourceCodeMkdirsFailed(VertxTestContext testContext) {
        Path rootFolder = Path.of("./rootFolder");
        String functionIdentifier = "funct";
        String code = "def main():\n\treturn 0\n";
        String fileName = "cloud_function.txt";
        Path sourceCodePath = Path.of(rootFolder.toString(), functionIdentifier, fileName);

        when(fileSystem.mkdirs(sourceCodePath.getParent().toString()))
            .thenReturn(Completable.error(IOException::new));
        when(fileSystem.writeFile(sourceCodePath.toString(), Buffer.buffer(code))).thenReturn(Completable.complete());

        packageSourceCode.composeSourceCode(rootFolder, functionIdentifier, code)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IOException.class);
                    testContext.completeNow();
                })
            );
    }
}