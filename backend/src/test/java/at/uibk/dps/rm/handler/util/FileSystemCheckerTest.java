package at.uibk.dps.rm.handler.util;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FileSystemCheckerTest {

    @RegisterExtension
    private static final RunTestOnContext rtoc = new RunTestOnContext();

    private FileSystemChecker fileSystemChecker;

    @Mock
    private FilePathService filePathService;

    @BeforeEach
    void initTest() {
        rtoc.vertx();
        fileSystemChecker = new FileSystemChecker(filePathService);
    }

    @Test
    void checkTemplatePathExistsTrue(VertxTestContext testContext) {
        String templatePath = "/template/fileexists.py";

        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(true));

        fileSystemChecker.checkTemplatePathExists(templatePath)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkTemplatePathExistsFalse(VertxTestContext testContext) {
        String templatePath = "/template/filenotexists.py";

        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(false));

        fileSystemChecker.checkTemplatePathExists(templatePath)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkGetTemplateFile(VertxTestContext testContext) {
        String templatePath = "/template/fileexists.py";
        String runtimeTemplate = "def main(): \n\treturn True";

        when(filePathService.getRuntimeTemplate(templatePath)).thenReturn(Single.just(runtimeTemplate));

        fileSystemChecker.checkGetFileTemplate(templatePath)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(runtimeTemplate);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkGetTemplateFileNotFound(VertxTestContext testContext) {
        String templatePath = "/template/filenotexists.py";

        when(filePathService.getRuntimeTemplate(templatePath)).thenReturn(Single.error(IOException::new));

        fileSystemChecker.checkGetFileTemplate(templatePath)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(IOException.class);
                    testContext.completeNow();
                }));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkLockFileExists(boolean exists, VertxTestContext testContext) {
        String lockFilePath = "./lockfile";

        when(filePathService.tfLocFileExists(lockFilePath)).thenReturn(Single.just(exists));

        fileSystemChecker.checkTFLockFileExists(lockFilePath)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(exists);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
