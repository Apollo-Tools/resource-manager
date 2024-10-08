package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Implements tests for the {@link ServiceDeployFileService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceDeployFileServiceTest {

    private final Path rootFolder = Path.of("temp", "deployment_1");


    private final Deployment deployment = TestDeploymentProvider.createDeployment(1L);

    @Test
    void getProviderString(Vertx vertx) {
        ServiceDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, deployment);

        assertThrows(NotImplementedException.class, service::getProviderString);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "false, true",
        "true, false"
    })
    void getMainFileContent(boolean hasExternalIp, boolean isNode, Vertx vertx) {
        Resource resource = TestResourceProvider.createResourceContainer(1L, "localhost", hasExternalIp);
        if (isNode) {
            Resource mainResource = TestResourceProvider
                .createResourceContainer(2L, "localhost", hasExternalIp);
            resource = TestResourceProvider.createSubResource(1L, "node1", (MainResource) mainResource);
        }
        ServiceDeployFileService service = TestFileServiceProvider
            .createContainerDeployFileService(vertx.fileSystem(), rootFolder, resource, deployment);
        String configPath = Path.of("tmp", "kubeconfig", resource.getMain().getName())
            .toAbsolutePath().toString().replace("\\", "/");

        String result = service.getMainFileContent();

        assertThat(result).isEqualTo(
                "module \"deployment_1\" {\n" +
                "  source = \"../../../terraform/k8s/deployment\"\n" +
                "  config_path = \"" + configPath + "\"\n" +
                "  config_context = \"k8s-context\"\n" +
                "  namespace = \"default\"\n" +
                "  name = \"test\"\n" +
                "  image = \"test:latest\"\n" +
                "  deployment_id = 1\n" +
                "  resource_deployment_id = 1\n" +
                "  service_id = 22\n" +
                "  replicas = 1\n" +
                "  cpu = \"13.37\"\n" +
                "  memory = \"128M\"\n" +
                "  ports = [{container_port = 80, service_port = 8000}]\n" +
                "  service_type = \"NodePort\"\n" +
                "  external_ip = \"" + (hasExternalIp ? "0.0.0.0" : "") + "\"\n" +
                "  hostname = " + (isNode ? "\"node1\"" : null) + "\n" +
                "  image_pull_secrets = [\"regcred\"]\n" +
                "  volume_mounts = [{name:\"vm\",mountPath:\"/build\",sizeMegaBytes:100}]\n" +
                "  env_vars = [{name:\"env_var\",value:\"value\"}]\n" +
                "}\n"
        );
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        ServiceDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getOutputFileContent(Vertx vertx) {
        ServiceDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo("output \"service_deployment_1\" {\n" +
            "  value = {\n" +
            "    service: module.deployment_1.service_info\n" +
            "    pods: module.deployment_1.pods_info\n" +
            "  }\n" +
            "}\n");
    }
}
