package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class MainFileServiceTest {

    @Test
    void getProviderString(Vertx vertx) {
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), new ArrayList<>());
        String result = service.getProviderString();

        assertThat(result).isEqualTo(
            "terraform {\n" +
            "  required_providers {\n" +
            "    aws = {\n" +
            "      source  = \"hashicorp/aws\"\n" +
            "      version = \"~> 4.16\"\n" +
            "    }\n" +
            "    kubernetes = {\n" +
            "      source = \"hashicorp/kubernetes\"\n" +
            "      version = \"2.20.0\"\n" +
            "    }\n" +
            "  }\n" +
            "  required_version = \">= 1.2.0\"\n" +
            "}\n");
    }

    @Test
    void getLocalModulesString(Vertx vertx) {
        TerraformModule m1 = new TerraformModule(CloudProvider.AWS, "m1");
        TerraformModule m2 = new TerraformModule(CloudProvider.AWS, "m2");
        TerraformModule m3 = new TerraformModule(CloudProvider.EDGE, "m3");
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1, m2, m3));
        String result = service.getLocalModulesString();

        assertThat(result).isEqualTo(
            "module \"m1\" {\n" +
                "  source = \"./m1\"\n" +
                "  access_key = var.aws_access_key\n" +
                "  secret_access_key = var.aws_secret_access_key\n" +
                "  session_token = var.aws_session_token\n" +
                "}\n" +
                "module \"m2\" {\n" +
                "  source = \"./m2\"\n" +
                "  access_key = var.aws_access_key\n" +
                "  secret_access_key = var.aws_secret_access_key\n" +
                "  session_token = var.aws_session_token\n" +
                "}\n" +
                "module \"edge\" {\n" +
                "  source = \"./edge\"\n" +
                "  login_data = var.edge_login_data\n" +
                "}\n");
    }

    @Test
    void getMainFileContent(Vertx vertx) {
        TerraformModule m1 = new TerraformModule(CloudProvider.AWS, "m1");
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1));
        String result = service.getMainFileContent();

        assertThat(result).isEqualTo(
            "terraform {\n" +
                "  required_providers {\n" +
                "    aws = {\n" +
                "      source  = \"hashicorp/aws\"\n" +
                "      version = \"~> 4.16\"\n" +
                "    }\n" +
                "    kubernetes = {\n" +
                "      source = \"hashicorp/kubernetes\"\n" +
                "      version = \"2.20.0\"\n" +
                "    }\n" +
                "  }\n" +
                "  required_version = \">= 1.2.0\"\n" +
                "}\n" +
                "module \"m1\" {\n" +
                "  source = \"./m1\"\n" +
                "  access_key = var.aws_access_key\n" +
                "  secret_access_key = var.aws_secret_access_key\n" +
                "  session_token = var.aws_session_token\n" +
                "}\n");
    }


    @Test
    void getVariablesFileContent(Vertx vertx) {
        TerraformModule m1 = new TerraformModule(CloudProvider.AWS, "m1");
        TerraformModule m2 = new TerraformModule(CloudProvider.AWS, "m2");
        TerraformModule m3 = new TerraformModule(CloudProvider.EDGE, "m3");
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1, m2, m3));
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo(
            "variable \"aws_access_key\" {\n" +
                "  type = string\n" +
                "  default = \"\"\n" +
                "}\n" +
                "variable \"aws_secret_access_key\" {\n" +
                "  type = string\n" +
                "  default = \"\"\n" +
                "}\n" +
                "variable \"aws_session_token\" {\n" +
                "  type = string\n" +
                "  default = \"\"\n" +
                "}\n" +
                "variable \"edge_login_data\" {\n" +
                "  type = list(object({\n" +
                "    auth_user = string\n" +
                "    auth_pw = string\n" +
                "  }))\n" +
                "}\n");
    }



    @Test
    public void getOutputsFileContentAllTypes(Vertx vertx) {
        TerraformModule m1 = new TerraformModule(CloudProvider.AWS, "m1");
        m1.setHasVM(true);
        TerraformModule m2 = new TerraformModule(CloudProvider.AWS, "m2");
        m2.setHasFaas(true);
        TerraformModule m3 = new TerraformModule(CloudProvider.EDGE, "m3");
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1, m2, m3));
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
            "output \"function_urls\" {\n" +
                "   value = merge(module.m2.function_urls,)\n" +
                "}\n" +
                "output \"vm_urls\" {\n" +
                "  value = merge(module.m1.vm_urls,)\n" +
                "}\n" +
                "output \"edge_urls\" {\n" +
                "  value = module.edge.edge_urls\n" +
                "}\n");
    }

    @Test
    public void getOutputsFileContentNone(Vertx vertx) {
        TerraformModule m1 = new TerraformModule(CloudProvider.AWS, "m1");
        TerraformModule m2 = new TerraformModule(CloudProvider.AWS, "m2");
        TerraformModule m3 = new TerraformModule(CloudProvider.AWS, "m3");
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1, m2, m3));
        String result = service.getOutputString();

        assertThat(result).isEqualTo(
            "output \"function_urls\" {\n" +
                "   value = merge()\n" +
                "}\n" +
                "output \"vm_urls\" {\n" +
                "  value = merge()\n" +
                "}\n" +
                "output \"edge_urls\" {\n" +
                "  value = {}\n" +
                "}\n");
    }

}
