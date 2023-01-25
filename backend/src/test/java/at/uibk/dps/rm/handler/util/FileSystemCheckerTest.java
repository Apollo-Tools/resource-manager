package at.uibk.dps.rm.handler.util;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FileSystemCheckerTest {

    private FileSystemChecker fileSystemChecker;

    @Mock
    private FilePathService filePathService;

    @BeforeEach
    void initTest() {
        fileSystemChecker = new FileSystemChecker(filePathService);
    }

    @Test
    void checkTemplatePathExistsTrue(VertxTestContext testContext) {
        String templatePath = "./filepathtest/filepathtest.py";

        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(true));

        fileSystemChecker.checkTemplatePathExists(templatePath)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(filePathService).templatePathExists(templatePath);
        testContext.completeNow();
    }

    @Test
    void checkTemplatePathExistsFalse(VertxTestContext testContext) {
        String templatePath = "./filepathtest/filepathtest.py";

        when(filePathService.templatePathExists(templatePath)).thenReturn(Single.just(false));

        fileSystemChecker.checkTemplatePathExists(templatePath)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
