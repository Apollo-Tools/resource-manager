package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.module.FaasModule;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.file.FileSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link TerraformFileService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class TerraformFileServiceTest {

    @Mock
    FileSystem fileSystem;

    @Test
    void setupDirectory(VertxTestContext testContext) {
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        MainFileService service = TestFileServiceProvider.createMainFileService(fileSystem, List.of(m1));
        Path rootFolder = Path.of("temp", "test");
        when(fileSystem.mkdirs(rootFolder.toString())).thenReturn(Completable.complete());
        doReturn(Completable.complete())
            .when(fileSystem).writeFile(eq(Path.of(rootFolder.toString(), "main.tf").toString()), any());
        doReturn(Completable.complete())
            .when(fileSystem).writeFile(eq(Path.of(rootFolder.toString(), "variables.tf").toString()), any());
        doReturn(Completable.complete())
            .when(fileSystem).writeFile(eq(Path.of(rootFolder.toString(), "outputs.tf").toString()), any());

        service.setUpDirectory()
            .blockingSubscribe(() -> {},
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
        testContext.completeNow();
    }

    @Test
    void setupDirectoryEmpty(VertxTestContext testContext) {
        Path rootFolder = Path.of("temp", "test");
        EmptyFileService service = new EmptyFileService(fileSystem, rootFolder);
        when(fileSystem.mkdirs(rootFolder.toString())).thenReturn(Completable.complete());

        service.setUpDirectory()
            .blockingSubscribe(() -> {},
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
        testContext.completeNow();
    }

    @Test
    void deleteAllDirs(VertxTestContext testContext) {
        Path rootFolder = Path.of("temp", "test");
        when(fileSystem.deleteRecursive(rootFolder.toString(), true)).thenReturn(Completable.complete());

        TerraformFileService.deleteAllDirs(fileSystem, rootFolder)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    /**
     * Test implementation of the {@link TerraformFileService} class with an empty file content.
     */
    private static class EmptyFileService extends TerraformFileService {

        public EmptyFileService(FileSystem fileSystem, Path rootFolder) {
            super(fileSystem, rootFolder);
        }

        @Override
        protected String getProviderString() {
            return "";
        }

        @Override
        protected String getMainFileContent() {
            return "";
        }

        @Override
        protected String getVariablesFileContent() {
            return "";
        }

        @Override
        protected String getOutputsFileContent() {
            return "";
        }
    }
}
