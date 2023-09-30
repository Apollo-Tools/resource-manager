package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ContainerDeployFileService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ContainerDeployFileServiceTest {

    private final Path rootFolder = Path.of("temp", "deployment_1");


    private final Deployment deployment = TestDeploymentProvider.createDeployment(1L);

    @Test
    void getProviderString(Vertx vertx) {
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getProviderString();

        assertThat(result).isEqualTo("terraform {\n" +
            "  required_providers {\n" +
            "    kubernetes = {\n" +
            "      source = \"hashicorp/kubernetes\"\n" +
            "      version = \"2.20.0\"\n" +
            "    }\n" +
            "  }\n" +
            "  required_version = \">= 1.2.0\"\n" +
            "}\n");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getMainFileContent(boolean hasExternalIp, Vertx vertx) {
        Resource resource = TestResourceProvider.createResourceContainer(1L, "localhost", hasExternalIp);
        ContainerDeployFileService service = TestFileServiceProvider
            .createContainerDeployFileService(vertx.fileSystem(), rootFolder, resource, deployment);
        String configPath = Path.of("tmp", "kubeconfig", "mainresource1").toAbsolutePath().toString()
            .replace("\\", "/");

        String result = service.getMainFileContent();

        assertThat(result).isEqualTo(
            "terraform {\n" +
                "  required_providers {\n" +
                "    kubernetes = {\n" +
                "      source = \"hashicorp/kubernetes\"\n" +
                "      version = \"2.20.0\"\n" +
                "    }\n" +
                "  }\n" +
                "  required_version = \">= 1.2.0\"\n" +
                "}\n" +
                "module \"deployment_1_22\" {\n" +
                "  source = \"../../../../terraform/k8s/deployment\"\n" +
                "  config_path = \"" + configPath + "\"\n" +
                "  config_context = \"k8s-context\"\n" +
                "  namespace = \"default\"\n" +
                "  name = \"test\"\n" +
                "  image = \"test:latest\"\n" +
                "  deployment_id = 1\n" +
                "  replicas = 1\n" +
                "  cpu = \"13.37\"\n" +
                "  memory = \"128M\"\n" +
                "  ports = [{container_port = 80, service_port = 8000}]\n" +
                "  service_type = \"NodePort\"\n" +
                "  external_ip = \"" + (hasExternalIp ? "0.0.0.0" : "") + "\"\n" +
                "  hostname = null\n" +
                "  image_pull_secrets = [\"regcred\"]\n" +
                "  volume_mounts = [{name:\"vm\",mountPath:\"/build\",sizeMegaBytes:100}]\n" +
                "  env_vars = [{name:\"env_var\",value:\"value\"}]\n" +
                "}\n"
        );
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getOutputFileContent(Vertx vertx) {
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, deployment);
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo("output \"deployment_data\" {\n" +
            "  value = {\n" +
            "    service: module.deployment_33_22.service_info\n" +
            "    pods: module.deployment_33_22.pods_info\n" +
            "  }\n" +
            "}");
    }
}
