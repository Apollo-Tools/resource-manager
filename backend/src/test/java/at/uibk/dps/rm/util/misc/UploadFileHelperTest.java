package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.TestConfigProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link UploadFileHelper} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class UploadFileHelperTest {

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    private ConfigDTO config;

    @BeforeEach
    void initTest() {
        config = TestConfigProvider.getConfigDTO();
    }

    @Test
    void persistUploadFile(VertxTestContext testContext) {
        String fileName = "file";
        Path tempPath = Path.of(config.getUploadTempDirectory(), fileName);
        Path persistPath = Path.of(config.getUploadPersistDirectory(), fileName);

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.copy(tempPath.toString(), persistPath.toString()))
            .thenReturn(Completable.complete());

        try (MockedConstruction<ConfigUtility> ignore = Mockprovider.mockConfig(config)) {
               UploadFileHelper.persistUploadedFile(vertx, fileName)
                   .subscribe(testContext::completeNow,
                       throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteFile(boolean fileExists, VertxTestContext testContext) {
        String fileName = "file";
        Path filePath = Path.of(config.getUploadPersistDirectory(), fileName);

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.exists(filePath.toString())).thenReturn(Single.just(fileExists));
        if (fileExists) {
            when(fileSystem.delete(filePath.toString())).thenReturn(Completable.complete());
        }

        try (MockedConstruction<ConfigUtility> ignore = Mockprovider.mockConfig(config)) {
            UploadFileHelper.deleteFile(vertx, fileName)
                .subscribe(() -> testContext.verify(() -> {
                    verify(fileSystem, times(fileExists ? 1 : 0)).delete(filePath.toString());
                    testContext.completeNow();
                }), throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @Test
    void updateFile(VertxTestContext testContext) {
        String oldFile = "old-file";
        String newFile = "new-file";
        Path oldFilePath = Path.of(config.getUploadPersistDirectory(), oldFile);
        Path tempPath = Path.of(config.getUploadTempDirectory(), newFile);
        Path newFilePath = Path.of(config.getUploadPersistDirectory(), newFile);

        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.exists(oldFilePath.toString())).thenReturn(Single.just(true));
        when(fileSystem.delete(oldFilePath.toString())).thenReturn(Completable.complete());
        when(vertx.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.copy(tempPath.toString(), newFilePath.toString()))
            .thenReturn(Completable.complete());

        try (MockedConstruction<ConfigUtility> ignore = Mockprovider.mockConfig(config)) {
            UploadFileHelper.updateFile(vertx, oldFile, newFile)
                .subscribe(testContext::completeNow, throwable -> testContext.failNow("method has thrown exception"));
        }
    }
}
