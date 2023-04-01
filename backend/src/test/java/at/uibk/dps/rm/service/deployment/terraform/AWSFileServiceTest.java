package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.testutil.objectprovider.TestFileServiceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class AWSFileServiceTest {

    @Test
    void getProviderString(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasVMEdge(vertx.fileSystem());
        String result = service.getProviderString();

        assertThat(result).isEqualTo("provider \"aws\" {\n" +
            "  access_key = var.access_key\n" +
            "  secret_key = var.secret_access_key\n" +
            "  token = var.session_token\n" +
            "  region = \"us-east-1\"\n" +
            "}\n");
    }

    @Test
    void getFunctionsModuleStringFaasVM(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasVMEdge(vertx.fileSystem());
        String result = service.getFunctionsModulString();

        String f1 = "foo1_python39";
        String r1f1 = "r1_" + f1 + "_1";
        String functionsPath = Paths.get("temp\\test\\functions\\").toAbsolutePath().toString();
        String r1f1Path = (functionsPath + "\\" + f1 + ".zip").replace("\\", "/");
        assertThat(result).isEqualTo(String.format("module \"faas\" {\n" +
            "  source = \"../../../terraform/aws/faas\"\n" +
            "  names = [\"%s\",]\n" +
            "  paths = [\"%s\",]\n" +
            "  handlers = [\"main.handler\",]\n" +
            "  timeouts = [250.0,]\n" +
            "  memory_sizes = [512.0,]\n" +
            "  layers = [[],]\n" +
            "  runtimes = [\"python39\",]\n" +
            "  aws_role = \"LabRole\"\n" +
            "}\n", r1f1, r1f1Path));
    }

    @Test
    void getFunctionsModuleStringNoFaas(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceVMEdge(vertx.fileSystem());
        String result = service.getFunctionsModulString();
        assertThat(result).isEqualTo("");
    }

    @Test
    void getFunctionsModuleStringUnsupportedRuntime(Vertx vertx) {
        Runtime runtime = TestFunctionProvider.createRuntime(1L, "unknown");
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasVMEdge(vertx.fileSystem(),runtime);
        assertThrows(RuntimeNotSupportedException.class, service::getFunctionsModulString);
    }

    @Test
    void getVMModulesString(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasVMEdge(vertx.fileSystem());

        String f1 = "foo1_python39", f2 = "foo2_python39";
        String r2f1 = "r2_" + f1, r2f2 = "r2_" + f2;
        String result = service.getVmModulesString();
        assertThat(result).isEqualTo(String.format(
            "module \"vm\" {\n" +
            "  source         = \"../../../terraform/aws/vm\"\n" +
            "  reservation    = \"1\"\n" +
            "  names          = [\"resource_2\",]\n" +
            "  instance_types = [\"t2.micro\",]\n" +
            "  vpc_id         = \"vpc-id\"\n" +
            "  subnet_id      = \"subnet-id\"\n" +
            "}\n" +
            "module \"%s\" {\n" +
            "  openfaas_depends_on = module.vm\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"%s_1\"\n" +
            "  image = \"dockerUser/%s\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.vm.vm_props[\"resource_2\"]\n" +
            "}\n" +
            "module \"%s\" {\n" +
            "  openfaas_depends_on = module.vm\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"%s_1\"\n" +
            "  image = \"dockerUser/%s\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.vm.vm_props[\"resource_2\"]\n" +
            "}\n", r2f1, r2f1, f1, r2f2, r2f2, f2));
    }

    @Test
    void getVMModulesStringNoVM(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasEdge(vertx.fileSystem());
        String result = service.getVmModulesString();
        assertThat(result).isEqualTo("");
    }

    @Test
    void getVariablesFileContent(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasVMEdge(vertx.fileSystem());
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
            "}\n"
        );
    }

    @Test
    void getOutputsFileContentFaasVM(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasVMEdge(vertx.fileSystem());
        service.getMainFileContent();
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
            "output \"function_urls\" {\n" +
            "  value = module.faas.function_urls\n" +
            "}\n" +
            "output \"vm_props\" {\n" +
            "  value = module.vm.vm_props\n" +
            "  sensitive = true\n" +
            "}\n" +
            "output \"vm_urls\" {\n" +
            "  value = zipmap([\"r2_foo1_python39\",\"r2_foo2_python39\",], " +
                "[module.r2_foo1_python39.function_url,module.r2_foo2_python39.function_url,])\n" +
            "}\n"
        );
    }

    @Test
    void getOutputsFileContentFaasEdge(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasEdge(vertx.fileSystem());
        service.getMainFileContent();
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
            "output \"function_urls\" {\n" +
            "  value = module.faas.function_urls\n" +
            "}\n"
        );
    }

    @Test
    void getOutputsFileContentVMEdge(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceVMEdge(vertx.fileSystem());
        service.getMainFileContent();
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo(
        "output \"vm_props\" {\n" +
            "  value = module.vm.vm_props\n" +
            "  sensitive = true\n" +
            "}\n" +
            "output \"vm_urls\" {\n" +
            "  value = zipmap([\"r2_foo1_python39\",\"r2_foo2_python39\",\"r3_foo1_python39\",], [module" +
                ".r2_foo1_python39.function_url,module.r2_foo2_python39.function_url,module.r3_foo1_python39" +
            ".function_url,])\n" +
            "}\n"
        );
    }

    @Test
    void getOutputsFileContentEdge(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceEdge(vertx.fileSystem());
        service.getMainFileContent();
        String result = service.getOutputsFileContent();

        assertThat(result).isEqualTo("");
    }

    @Test
    void getMainFileContent(Vertx vertx) {
        AWSFileService service = TestFileServiceProvider.createAWSFileServiceFaasVMEdge(vertx.fileSystem());
        String result = service.getMainFileContent();

        assertThat(result).isEqualTo(
            "provider \"aws\" {\n" +
            "  access_key = var.access_key\n" +
            "  secret_key = var.secret_access_key\n" +
            "  token = var.session_token\n" +
            "  region = \"us-east-1\"\n" +
            "}\n" +
            "module \"faas\" {\n" +
            "  source = \"../../../terraform/aws/faas\"\n" +
            "  names = [\"r1_foo1_python39_1\",]\n" +
            "  paths = [\"C:/Users/matthiasga/Documents/resource-manager/backend/temp/test/functions/foo1_python39.zip\",]\n" +
            "  handlers = [\"main.handler\",]\n" +
            "  timeouts = [250.0,]\n" +
            "  memory_sizes = [512.0,]\n" +
            "  layers = [[],]\n" +
            "  runtimes = [\"python39\",]\n" +
            "  aws_role = \"LabRole\"\n" +
            "}\n" +
            "module \"vm\" {\n" +
            "  source         = \"../../../terraform/aws/vm\"\n" +
            "  reservation    = \"1\"\n" +
            "  names          = [\"resource_2\",]\n" +
            "  instance_types = [\"t2.micro\",]\n" +
            "  vpc_id         = \"vpc-id\"\n" +
            "  subnet_id      = \"subnet-id\"\n" +
            "}\n" +
            "module \"r2_foo1_python39\" {\n" +
            "  openfaas_depends_on = module.vm\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo1_python39_1\"\n" +
            "  image = \"dockerUser/foo1_python39\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.vm.vm_props[\"resource_2\"]\n" +
            "}\n" +
            "module \"r2_foo2_python39\" {\n" +
            "  openfaas_depends_on = module.vm\n" +
            "  source = \"../../../terraform/openfaas\"\n" +
            "  name = \"r2_foo2_python39_1\"\n" +
            "  image = \"dockerUser/foo2_python39\"\n" +
            "  basic_auth_user = \"admin\"\n" +
            "  vm_props = module.vm.vm_props[\"resource_2\"]\n" +
            "}\n"
        );
    }
}
