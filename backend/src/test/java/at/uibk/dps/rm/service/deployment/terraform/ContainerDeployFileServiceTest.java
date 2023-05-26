package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ContainerDeployFileServiceTest {

    private final Path rootFolder = Paths.get("temp\\reservation_1");


    private final Reservation reservation = TestReservationProvider.createReservation(1L);

    @Test
    void getProviderString(Vertx vertx) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, reservation);
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
        Reservation reservation = TestReservationProvider.createReservation(1L);
        Resource resource = TestResourceProvider.createResourceContainer(1L, "localhost", hasExternalIp);
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, resource,
                reservation);
        String configPath = Path.of(rootFolder.getParent().toString(), "config").toAbsolutePath().toString()
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
                "  image = \"test:latest\"\n" +
                "  reservation_id = 1\n" +
                "  replicas = 1\n" +
                "  cpu = \"0.1\"\n" +
                "  memory = \"1024M\"\n" +
                "  ports = [{container_port = 80, service_port = 8000}]\n" +
                "  service_type = \"NodePort\"\n" +
                "  external_ip = \"" + (hasExternalIp ? "0.0.0.0" : "") + "\"\n" +
                "}\n"
        );
    }
    @Test
    void getCredentialVariablesString(Vertx vertx) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, reservation);
        String result = service.getCredentialVariablesString();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, reservation);
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getOutputFileContent(Vertx vertx) {
        ContainerDeployFileService service =
            TestFileServiceProvider.createContainerDeployFileService(vertx.fileSystem(), rootFolder, reservation);
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo("");
    }
}
