package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class EdgeFileServiceTest {
    @Test
    void getProviderString(Vertx vertx) {
        EdgeFileService service = TestFileServiceProvider.createEdgeFileServiceEdge(vertx.fileSystem());
        String result = service.getProviderString();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getMainFileContent(Vertx vertx) {
        EdgeFileService service = TestFileServiceProvider.createEdgeFileServiceEdge(vertx.fileSystem());
        String result = service.getMainFileContent();

        assertThat(result).isEqualTo(
            "module \"r1_foo1_python39\" {\n" +
            "  openfaas_depends_on = 0\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r1_foo1_python39_1\"\n" +
            "  image = \"dockerUser/foo1_python39\"\n" +
            "  basic_auth_user = var.login_data[0].auth_user\n" +
            "  vm_props = {\n" +
            "    gateway_url = \"http://localhost:8081\"\n" +
            "    auth_password = var.login_data[0].auth_pw\n" +
            "  }\n" +
            "}\n" +
            "module \"r2_foo1_python39\" {\n" +
            "  openfaas_depends_on = 0\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo1_python39_1\"\n" +
            "  image = \"dockerUser/foo1_python39\"\n" +
            "  basic_auth_user = var.login_data[1].auth_user\n" +
            "  vm_props = {\n" +
            "    gateway_url = \"http://localhost:8082\"\n" +
            "    auth_password = var.login_data[1].auth_pw\n" +
            "  }\n" +
            "}\n" +
            "module \"r2_foo2_python39\" {\n" +
            "  openfaas_depends_on = 0\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo2_python39_1\"\n" +
            "  image = \"dockerUser/foo2_python39\"\n" +
            "  basic_auth_user = var.login_data[1].auth_user\n" +
            "  vm_props = {\n" +
            "    gateway_url = \"http://localhost:8082\"\n" +
            "    auth_password = var.login_data[1].auth_pw\n" +
            "  }\n" +
            "}\n" +
            "module \"r3_foo1_python39\" {\n" +
            "  openfaas_depends_on = 0\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r3_foo1_python39_1\"\n" +
            "  image = \"dockerUser/foo1_python39\"\n" +
            "  basic_auth_user = var.login_data[2].auth_user\n" +
            "  vm_props = {\n" +
            "    gateway_url = \"http://localhost:8083\"\n" +
            "    auth_password = var.login_data[2].auth_pw\n" +
            "  }\n" +
            "}\n"
        );
    }

    @Test
    void getMainFileContentNoEdge(Vertx vertx) {
        EdgeFileService service = TestFileServiceProvider.createEdgeFileServiceFaasVM(vertx.fileSystem());
        String result = service.getMainFileContent();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getCredentialVariablesString(Vertx vertx) {
        EdgeFileService service = TestFileServiceProvider.createEdgeFileServiceEdge(vertx.fileSystem());
        String result = service.getCredentialVariablesString();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        EdgeFileService service = TestFileServiceProvider.createEdgeFileServiceEdge(vertx.fileSystem());
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo(
            "variable \"login_data\" {\n" +
            "  type = list(object({\n" +
            "    auth_user = string\n" +
            "    auth_pw = string\n" +
            "  }))\n" +
            "}"
        );
    }

    @Test
    void getOutputFileContent(Vertx vertx) {
        EdgeFileService service = TestFileServiceProvider.createEdgeFileServiceEdge(vertx.fileSystem());
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
            "output \"edge_urls\" {\n" +
                "  value = zipmap([\"r1_foo1_python39\",\"r2_foo1_python39\",\"r2_foo2_python39\"," +
                "\"r3_foo1_python39\",], [module.r1_foo1_python39.function_url,module.r2_foo1_python39.function_url," +
                "module.r2_foo2_python39.function_url,module.r3_foo1_python39.function_url,])\n" +
                "}\n");
    }

    @Test
    void getOutputFileContentNoEdge(Vertx vertx) {
        EdgeFileService service = TestFileServiceProvider.createEdgeFileServiceFaasVM(vertx.fileSystem());
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo("");
    }
}
