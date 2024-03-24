package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.module.FaasModule;
import at.uibk.dps.rm.entity.deployment.module.ModuleType;
import at.uibk.dps.rm.entity.deployment.module.ServiceModule;
import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link MainFileService} class.
 *
 * @author matthi-g
 */
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
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "r2");
        Region r3 = TestResourceProviderProvider.createRegion(3L, "r3");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        TerraformModule m2 = new FaasModule(ResourceProviderEnum.AWS, r2);
        TerraformModule m3 = new FaasModule(ResourceProviderEnum.CUSTOM_EDGE, r3);
        TerraformModule m4 = new ServiceModule("container_prepull", ModuleType.SERVICE_PREPULL);
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(),
            List.of(m1, m2, m3, m4));
        String result = service.getLocalModulesString();

        assertThat(result).isEqualTo(
            "module \"aws_r1\" {\n" +
                "  source = \"./aws_r1\"\n" +
                "  access_key = var.aws_access_key\n" +
                "  secret_access_key = var.aws_secret_access_key\n" +
                "  session_token = var.aws_session_token\n" +
                "  openfaas_login_data = var.openfaas_login_data\n" +
                "}\n" +
                "module \"aws_r2\" {\n" +
                "  source = \"./aws_r2\"\n" +
                "  access_key = var.aws_access_key\n" +
                "  secret_access_key = var.aws_secret_access_key\n" +
                "  session_token = var.aws_session_token\n" +
                "  openfaas_login_data = var.openfaas_login_data\n" +
                "}\n" +
                "module \"custom-edge_r3\" {\n" +
                "  source = \"./custom-edge_r3\"\n" +
                "  access_key = var.custom_edge_access_key\n" +
                "  secret_access_key = var.custom_edge_secret_access_key\n" +
                "  session_token = var.custom_edge_session_token\n" +
                "  openfaas_login_data = var.openfaas_login_data\n" +
                "}\n" +
                "module \"container\" {\n" +
                "  source = \"./container\"\n" +
                "}\n");
    }

    @Test
    void getMainFileContent(Vertx vertx) {
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
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
                "module \"aws_r1\" {\n" +
                "  source = \"./aws_r1\"\n" +
                "  access_key = var.aws_access_key\n" +
                "  secret_access_key = var.aws_secret_access_key\n" +
                "  session_token = var.aws_session_token\n" +
                "  openfaas_login_data = var.openfaas_login_data\n" +
                "}\n");
    }


    @Test
    void getVariablesFileContent(Vertx vertx) {
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "r2");
        Region r3 = TestResourceProviderProvider.createRegion(3L, "r3");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        TerraformModule m2 = new FaasModule(ResourceProviderEnum.AWS, r2);
        TerraformModule m3 = new FaasModule(ResourceProviderEnum.CUSTOM_EDGE, r3);
        TerraformModule m4 = new ServiceModule("container_prepull", ModuleType.SERVICE_PREPULL);
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1, m2,
            m3, m4));
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
                "variable \"custom_edge_access_key\" {\n" +
                "  type = string\n" +
                "  default = \"\"\n" +
                "}\n" +
                "variable \"custom_edge_secret_access_key\" {\n" +
                "  type = string\n" +
                "  default = \"\"\n" +
                "}\n" +
                "variable \"custom_edge_session_token\" {\n" +
                "  type = string\n" +
                "  default = \"\"\n" +
                "}\n" +
                "variable \"openfaas_login_data\" {\n" +
                "  type = map(object({\n" +
                "      auth_user = string\n" +
                "      auth_pw = string\n" +
                "  }))\n" +
                "  default = {}\n" +
                "}\n");
    }



    @Test
    void getOutputsFileContentAllTypes(Vertx vertx) {
        Region r1 = TestResourceProviderProvider.createRegion(1L, "r1");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "r2");
        Region r3 = TestResourceProviderProvider.createRegion(3L, "r3");
        TerraformModule m1 = new FaasModule(ResourceProviderEnum.AWS, r1);
        TerraformModule m2 = new FaasModule(ResourceProviderEnum.AWS, r2);
        TerraformModule m3 = new FaasModule(ResourceProviderEnum.CUSTOM_EDGE, r3);
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1, m2, m3));
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
            "output \"resource_output\" {\n" +
                "   value = merge(module.aws_r1.resource_output,module.aws_r2.resource_output," +
                "module.custom-edge_r3.resource_output,)\n" +
                "}\n");
    }

    @Test
    void getOutputsFileContentNone(Vertx vertx) {
        TerraformModule m1 = new ServiceModule("container_prepull", ModuleType.SERVICE_PREPULL);
        MainFileService service = TestFileServiceProvider.createMainFileService(vertx.fileSystem(), List.of(m1));
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
            "output \"resource_output\" {\n" +
                "   value = merge()\n" +
                "}\n");
    }

}
