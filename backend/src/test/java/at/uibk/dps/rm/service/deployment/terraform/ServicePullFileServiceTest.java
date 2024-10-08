package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.objectprovider.*;
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
 * Implements tests for the {@link ServicePullFileService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServicePullFileServiceTest {

    private final Path rootFolder = Paths.get("temp\\deployment_1");


    private final Deployment deployment = TestDeploymentProvider.createDeployment(1L);

    @Test
    void getProviderString(Vertx vertx) {
        ServicePullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getProviderString();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getMainFileContent(Vertx vertx) {
        Resource r1 = TestResourceProvider.createResourceContainer(1L, "localhost", true);
        Resource mainResource = TestResourceProvider
            .createResourceContainer(2L, "10.0.0.1", true);
        Resource r2 = TestResourceProvider.createSubResource(3L, "node1", (MainResource) mainResource);
        Service s1 = TestServiceProvider.createService(1L, "test1");
        Service s2 = TestServiceProvider.createService(1L, "test2");
        ServiceDeployment sr1 = TestServiceProvider.createServiceDeployment(1L, s1, r1);
        ServiceDeployment sr2 = TestServiceProvider.createServiceDeployment(1L, s2, r1);
        ServiceDeployment sr3 = TestServiceProvider.createServiceDeployment(1L, s1, r2);
        ServicePullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, deployment,
                List.of(sr1, sr2, sr3));
        String configPath = Path.of("tmp", "kubeconfig", "mainresource").toAbsolutePath().toString()
            .replace("\\", "/");

        String result = service.getMainFileContent();

        assertThat(result).isEqualTo(
            "module \"pre_pull_1\" {\n" +
                "  source = \"../../../terraform/k8s/prepull\"\n" +
                "  deployment_id = 1\n" +
                "  config_path = \"" + configPath + "1\"\n" +
                "  namespace = \"default\"\n" +
                "  config_context = \"k8s-context\"\n" +
                "  images = [\"test2:latest\",\"test1:latest\"]\n" +
                "  timeout = \"2m\"\n" +
                "  hostname = null\n" +
                "  image_pull_secrets = [\"regcred\"]\n" +
                "}\n" +
                "module \"pre_pull_3\" {\n" +
                "  source = \"../../../terraform/k8s/prepull\"\n" +
                "  deployment_id = 1\n" +
                "  config_path = \"" + configPath + "2\"\n" +
                "  namespace = \"default\"\n" +
                "  config_context = \"k8s-context\"\n" +
                "  images = [\"test1:latest\"]\n" +
                "  timeout = \"2m\"\n" +
                "  hostname = \"node1\"\n" +
                "  image_pull_secrets = [\"regcred\"]\n" +
                "}\n"
        );
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        ServicePullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getOutputFileContent(Vertx vertx) {
        ServicePullFileService service =
            TestFileServiceProvider.createContainerPullFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo("");
    }
}
