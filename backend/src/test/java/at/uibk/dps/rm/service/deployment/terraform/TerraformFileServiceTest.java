package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
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
        TerraformModule m1 = new TerraformModule(CloudProvider.AWS, "m1");
        MainFileService service = TestFileServiceProvider.createMainFileService(fileSystem, List.of(m1));
        Path rootFolder = Path.of("temp", "test");
        when(fileSystem.mkdirs(rootFolder.toString())).thenReturn(Completable.complete());
        when(fileSystem.writeFile(eq(Path.of(rootFolder.toString(), "main.tf").toString()), any()))
            .thenReturn(Completable.complete());
        when(fileSystem.writeFile(eq(Path.of(rootFolder.toString(), "variables.tf").toString()), any()))
            .thenReturn(Completable.complete());
        when(fileSystem.writeFile(eq(Path.of(rootFolder.toString(), "outputs.tf").toString()), any()))
            .thenReturn(Completable.complete());

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
}
