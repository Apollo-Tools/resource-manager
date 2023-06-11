package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ContainerPullFileService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ContainerPullFileServiceTest {

    private final Path rootFolder = Paths.get("temp\\reservation_1");


    private final Deployment reservation = TestDeploymentProvider.createDeployment(1L);

    @Test
    void getProviderString(Vertx vertx) {
        ContainerPullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, reservation);
        String result = service.getProviderString();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getMainFileContent(Vertx vertx) {
        Resource r1 = TestResourceProvider.createResourceContainer(1L, "localhost", true);
        Resource r2 = TestResourceProvider.createResourceContainer(2L, "10.0.0.1", true);
        Service s1 = TestServiceProvider.createService(1L, "test1");
        Service s2 = TestServiceProvider.createService(1L, "test2");
        ServiceDeployment sr1 = TestServiceProvider.createServiceDeployment(1L, s1, r1);
        ServiceDeployment sr2 = TestServiceProvider.createServiceDeployment(1L, s2, r1);
        ServiceDeployment sr3 = TestServiceProvider.createServiceDeployment(1L, s1, r2);
        ContainerPullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, reservation,
                List.of(sr1, sr2, sr3));
        String configPath = Path.of(rootFolder.toString(), "config").toAbsolutePath().toString()
            .replace("\\", "/");

        String result = service.getMainFileContent();


        assertThat(result).isEqualTo(
            "module \"pre_pull_2default\" {\n" +
                "  source = \"../../../terraform/k8s/prepull\"\n" +
                "  reservation_id = 1\n" +
                "  config_path = \"" + configPath + "\"\n" +
                "  namespace = \"default\"\n" +
                "  config_context = \"k8s-context\"\n" +
                "  images = [\"test1:latest\"]\n" +
                "  timeout = \"2m\"\n" +
                "}\n" +
                "module \"pre_pull_1default\" {\n" +
                "  source = \"../../../terraform/k8s/prepull\"\n" +
                "  reservation_id = 1\n" +
                "  config_path = \"" + configPath + "\"\n" +
                "  namespace = \"default\"\n" +
                "  config_context = \"k8s-context\"\n" +
                "  images = [\"test1:latest\",\"test2:latest\"]\n" +
                "  timeout = \"2m\"\n" +
                "}\n"
        );
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        ContainerPullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, reservation);
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getOutputFileContent(Vertx vertx) {
        ContainerPullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, reservation);
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo("");
    }
}
