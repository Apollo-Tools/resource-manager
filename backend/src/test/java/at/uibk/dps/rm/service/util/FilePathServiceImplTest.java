package at.uibk.dps.rm.service.util;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FilePathServiceImplTest {

    private FilePathService filePathService;

    @Mock
    Vertx vertx;

    @Mock
    FileSystem fileSystem;

    @BeforeEach
    void initTest() {
        when(vertx.fileSystem()).thenReturn(fileSystem);
        filePathService = new FilePathServiceImpl(vertx);
    }

    @ParameterizedTest
    @ValueSource(strings = {"./filepathtest/filepathtest.py"})
    void checkTemplatePathExistsValid(String templatePath, VertxTestContext testContext) {
        when(fileSystem.exists(any())).thenReturn(Future.succeededFuture(true));

        filePathService.templatePathExists(templatePath)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(true);
                    testContext.completeNow();
                }))
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"./filepathtest/doesnotexist.py", ""})
    void checkTemplatePathDoesNotExist(String templatePath, VertxTestContext testContext) {
        when(fileSystem.exists(any())).thenReturn(Future.succeededFuture(false));

        filePathService.templatePathExists(templatePath)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(templatePath.isBlank());
                    testContext.completeNow();
                }))
            );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void tfLocFileExists(boolean exists, VertxTestContext testContext) {
        String tfPath = "./tfDir";
        when(fileSystem.exists(Paths.get(tfPath, ".terraform.lock.hcl").toString()))
            .thenReturn(Future.succeededFuture(exists));

        filePathService.tfLocFileExists(tfPath)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(exists);
                    testContext.completeNow();
                }))
            );
    }

    @Test
    void getRuntimeTemplate(VertxTestContext testContext) {
        String templatePath = ".\\path";
        String content = "def main():\n\treturn -1";
        when(fileSystem.readFile(templatePath))
            .thenReturn(Future.succeededFuture(Buffer.buffer().appendString(content)));

        filePathService.getRuntimeTemplate(templatePath)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo("def main():\n\treturn -1");
                    testContext.completeNow();
                }))
            );
    }
}
