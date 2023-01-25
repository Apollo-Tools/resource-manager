package at.uibk.dps.rm.service.util;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FilePathServiceImplTest {

    private FilePathService filePathService;

    @BeforeEach
    void initTest(Vertx vertx) {
        filePathService = new FilePathServiceImpl(vertx);
    }

    @ParameterizedTest
    @ValueSource(strings = {"./filepathtest/filepathtest.py", ""})
    void checkTemplatePathExistsValid(String templatePath, VertxTestContext testContext) {
        filePathService.templatePathExists(templatePath)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(true);
                    testContext.completeNow();
                }))
            );
    }

    @Test
    void checkTemplatePathExistsInValid(VertxTestContext testContext) {
        String templatePath = "./filepathtest/doesnotexist.py";

        filePathService.templatePathExists(templatePath)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(false);
                    testContext.completeNow();
                }))
            );
    }
}
