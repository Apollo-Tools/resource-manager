package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Implements tests for the {@link RegionFaasFileService} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RegionFaasFileServiceTest {

    @Test
    void getProviderString(Vertx vertx) {
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileServiceAllFaas(vertx.fileSystem());
        String result = service.getProviderString();

        assertThat(result).isEqualTo("provider \"aws\" {\n" +
            "  access_key = var.access_key\n" +
            "  secret_key = var.secret_access_key\n" +
            "  token = var.session_token == \"\" ? null : var.session_token\n" +
            "  region = \"us-east-1\"\n" +
            "}\n");
    }

    @Test
    void getProviderStringNonAWS(Vertx vertx) {
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileServiceAllFaas(vertx.fileSystem(),
            ResourceProviderEnum.CUSTOM_EDGE);
        String result = service.getProviderString();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getFunctionsModuleStringAllFaas(Vertx vertx) {
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileServiceAllFaas(vertx.fileSystem());
        String result = service.getFunctionsModuleString();

        String f1 = "foo1_python38";
        String functionsPath = Paths.get("build\\deployment_1\\functions").toAbsolutePath().toString();
        String r1f1Path = (functionsPath + "\\" + f1 + ".zip").replace("\\", "/");
        String layerPath = Paths.get("build\\deployment_1\\functions\\layers").toAbsolutePath().toString()
            .replace("\\", "/");
        assertThat(result).isEqualTo(String.format("module \"lambda\" {\n" +
            "  source = \"../../../terraform/aws/faas\"\n" +
            "  deployment_id = 1\n" +
            "  names = [\"r1_foo1_python38_1\",]\n" +
            "  paths = [\"%s\",]\n" +
            "  handlers = [\"lambda.handler\",]\n" +
            "  timeouts = [60,]\n" +
            "  memory_sizes = [128,]\n" +
            "  layers = {layers=[\"\",], path=\"%s\"}\n" +
            "  runtimes = [\"python3.8\",]\n" +
            "  deployment_roles = [\"labRole\",]\n" +
            "}\n" +
            "module \"ec2\" {\n" +
            "  source         = \"../../../terraform/aws/vm\"\n" +
            "  deployment_id  = 1\n" +
            "  names          = [\"resource_2\",]\n" +
            "  instance_types = [\"t2.micro\",]\n" +
            "  vpc_id         = \"vpc-id\"\n" +
            "  subnet_id      = \"subnet-id\"\n" +
            "}\n" +
            "module \"r2_foo1_python38\" {\n" +
            "  openfaas_depends_on = module.ec2\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo1_python38_1\"\n" +
            "  deployment_id = 1\n" +
            "  image = \"testuser/foo1_python38\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.ec2.vm_props[\"resource_2\"]\n" +
            "  timeout = 60\n" +
            "}\n" +
            "module \"r2_foo2_python38\" {\n" +
            "  openfaas_depends_on = module.ec2\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo2_python38_1\"\n" +
            "  deployment_id = 1\n" +
            "  image = \"testuser/foo2_python38\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.ec2.vm_props[\"resource_2\"]\n" +
            "  timeout = 60\n" +
            "}\n" +
            "module \"r3_foo1_python38\" {\n" +
            "  openfaas_depends_on = 0\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  deployment_id = 1\n" +
            "  name = \"r3_foo1_python38_1\"\n" +
            "  image = \"testuser/foo1_python38\"\n" +
            "  basic_auth_user = var.openfaas_login_data[\"r3\"].auth_user\n" +
            "  vm_props = {\n" +
            "    base_url = \"http://localhost\"\n" +
            "    metrics_port = 9100\n" +
            "    openfaas_port = 8080\n" +
            "    auth_password = var.openfaas_login_data[\"r3\"].auth_pw\n" +
            "  }\n" +
            "  timeout = 60\n" +
            "}\n", r1f1Path, layerPath));
    }

    @Test
    void getFunctionsModuleStringUnsupportedRuntime(Vertx vertx) {
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "unknown");
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileServiceAllFaas(vertx.fileSystem(),
            runtime, ResourceProviderEnum.AWS);
        assertThrows(RuntimeNotSupportedException.class, service::getFunctionsModuleString);
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileServiceAllFaas(vertx.fileSystem());
        String result = service.getVariablesFileContent();

        assertThat(result).isEqualTo(
            "variable \"access_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"secret_access_key\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"session_token\" {\n" +
            "  type = string\n" +
            "  default = \"\"\n" +
            "}\n" +
            "variable \"openfaas_login_data\" {\n" +
            "  type = map(object({\n" +
            "      auth_user = string\n" +
            "      auth_pw = string\n" +
            "  }))\n" +
            "  default = {}\n" +
            "}\n"
        );
    }

    @Test
    void getOutputsFileContentAllFaas(Vertx vertx) {
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileServiceAllFaas(vertx.fileSystem());
        service.getMainFileContent();
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
            "output \"resource_output\" {\n" +
            "  value = merge(module.lambda.resource_output, zipmap([\"r2_foo1_python38_1\",\"r2_foo2_python38_1\"," +
                "\"r3_foo1_python38_1\",], [module.r2_foo1_python38.resource_output,module.r2_foo2_python38.resource_output," +
                "module.r3_foo1_python38.resource_output,]))\n" +
            "}\n"
        );
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true",
        "true, false, false",
        "false, true, false",
        "false, false, true"
    })
    void getOutputsFileContentBlank(boolean blankLambda, boolean blankOpenFaas, boolean blankEc2, Vertx vertx) {
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L,
            ResourceProviderEnum.AWS.getValue());
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "python3.8");
        Region region = TestResourceProviderProvider.createRegion(1L, "us-east-1", resourceProvider);
        Resource r1, r2, r3;
        if (blankLambda && !blankOpenFaas && !blankEc2) {
            r1 = TestResourceProvider.createResourceEC2(1L, region, "t2.micro");
            r2 = TestResourceProvider.createResourceEC2(2L, region, "t2.micro");
            r3 = TestResourceProvider.createResourceOpenFaas(3L, region, "http://localhost",
                "user", "pw");
        } else if (!blankLambda && blankOpenFaas && !blankEc2) {
            r1 = TestResourceProvider.createResourceLambda(1L, region);
            r2 = TestResourceProvider.createResourceEC2(2L, region, "t2.micro");
            r3 = TestResourceProvider.createResourceLambda(3L, region);
        } else if (!blankLambda && !blankOpenFaas && blankEc2) {
            r1 = TestResourceProvider.createResourceLambda(1L, region);
            r2 = TestResourceProvider.createResourceOpenFaas(2L, region, "http://localhost",
                "user", "pw");
            r3 = TestResourceProvider.createResourceOpenFaas(3L, region, "http://localhost",
                "user", "pw");
        } else {
            r1 = TestResourceProvider.createResourceLambda(1L, region);
            r2 = TestResourceProvider.createResourceEC2(2L, region, "t2.micro");
            r3 = TestResourceProvider.createResourceOpenFaas(3L, region, "http://localhost",
                "user", "pw");
        }
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileService(vertx.fileSystem(), r1, r2,
            r3, runtime, region, ResourceProviderEnum.AWS);
        if (!blankLambda || !blankEc2 || !blankOpenFaas) {
            service.getMainFileContent();
        }
        String result = service.getOutputsFileContent();

        if (blankLambda && !blankOpenFaas && !blankEc2) {
            assertThat(result).isEqualTo(
                "output \"resource_output\" {\n" +
                    "  value = merge({}, zipmap([\"r1_foo1_python38_1\",\"r2_foo1_python38_1\"," +
                    "\"r2_foo2_python38_1\",\"r3_foo1_python38_1\",], [module.r1_foo1_python38.resource_output," +
                    "module.r2_foo1_python38.resource_output,module.r2_foo2_python38.resource_output," +
                    "module.r3_foo1_python38.resource_output,]))\n" +
                    "}\n"
            );
        } else if (!blankLambda && blankOpenFaas && !blankEc2) {
            assertThat(result).isEqualTo(
                "output \"resource_output\" {\n" +
                    "  value = merge(module.lambda.resource_output, zipmap([\"r2_foo1_python38_1\"," +
                    "\"r2_foo2_python38_1\",], [module.r2_foo1_python38.resource_output," +
                    "module.r2_foo2_python38.resource_output,]))\n" +
                    "}\n"
            );
        } else if (!blankLambda && !blankOpenFaas && blankEc2) {
            assertThat(result).isEqualTo(
                "output \"resource_output\" {\n" +
                    "  value = merge(module.lambda.resource_output, zipmap([\"r2_foo1_python38_1\"," +
                    "\"r2_foo2_python38_1\",\"r3_foo1_python38_1\",], [module.r2_foo1_python38.resource_output," +
                    "module.r2_foo2_python38.resource_output,module.r3_foo1_python38.resource_output,]))\n" +
                    "}\n"
            );
        } else {
            assertThat(result).isEqualTo(
                "output \"resource_output\" {\n" +
                    "  value = merge({}, {})\n" +
                    "}\n"
            );
        }
    }

    @Test
    void getMainFileContent(Vertx vertx) {
        String rootFolder = Paths.get("build\\deployment_1\\functions").toAbsolutePath().toString()
            .replace("\\", "/");
        String layerPath = Paths.get("build\\deployment_1\\functions\\layers").toAbsolutePath().toString()
            .replace("\\", "/");
        RegionFaasFileService service = TestFileServiceProvider.createRegionFaasFileServiceAllFaas(vertx.fileSystem());
        String result = service.getMainFileContent();
        assertThat(result).isEqualTo(
            "provider \"aws\" {\n" +
            "  access_key = var.access_key\n" +
            "  secret_key = var.secret_access_key\n" +
            "  token = var.session_token == \"\" ? null : var.session_token\n" +
            "  region = \"us-east-1\"\n" +
            "}\n" +
            "module \"lambda\" {\n" +
            "  source = \"../../../terraform/aws/faas\"\n" +
            "  deployment_id = 1\n" +
            "  names = [\"r1_foo1_python38_1\",]\n" +
            "  paths = [\"" + rootFolder + "/foo1_python38.zip\",]\n" +
            "  handlers = [\"lambda.handler\",]\n" +
            "  timeouts = [60,]\n" +
            "  memory_sizes = [128,]\n" +
            "  layers = {layers=[\"\",], path=\"" + layerPath + "\"}\n" +
            "  runtimes = [\"python3.8\",]\n" +
            "  deployment_roles = [\"labRole\",]\n" +
            "}\n" +
            "module \"ec2\" {\n" +
            "  source         = \"../../../terraform/aws/vm\"\n" +
            "  deployment_id  = 1\n" +
            "  names          = [\"resource_2\",]\n" +
            "  instance_types = [\"t2.micro\",]\n" +
            "  vpc_id         = \"vpc-id\"\n" +
            "  subnet_id      = \"subnet-id\"\n" +
            "}\n" +
            "module \"r2_foo1_python38\" {\n" +
            "  openfaas_depends_on = module.ec2\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo1_python38_1\"\n" +
            "  deployment_id = 1\n" +
            "  image = \"testuser/foo1_python38\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.ec2.vm_props[\"resource_2\"]\n" +
            "  timeout = 60\n" +
            "}\n" +
            "module \"r2_foo2_python38\" {\n" +
            "  openfaas_depends_on = module.ec2\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo2_python38_1\"\n" +
            "  deployment_id = 1\n" +
            "  image = \"testuser/foo2_python38\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.ec2.vm_props[\"resource_2\"]\n" +
            "  timeout = 60\n" +
            "}\n" +
            "module \"r3_foo1_python38\" {\n" +
            "  openfaas_depends_on = 0\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  deployment_id = 1\n" +
            "  name = \"r3_foo1_python38_1\"\n" +
            "  image = \"testuser/foo1_python38\"\n" +
            "  basic_auth_user = var.openfaas_login_data[\"r3\"].auth_user\n" +
            "  vm_props = {\n" +
            "    base_url = \"http://localhost\"\n" +
            "    metrics_port = 9100\n" +
            "    openfaas_port = 8080\n" +
            "    auth_password = var.openfaas_login_data[\"r3\"].auth_pw\n" +
            "  }\n" +
            "  timeout = 60\n" +
            "}\n"
        );
    }
}
