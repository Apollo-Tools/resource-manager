package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

public class MainFileService extends TerraformFileService {

    private final List<TerraformModule> modules;

    public MainFileService(FileSystem fs, Path rootFolder, List<TerraformModule> modules) {
        super(fs, rootFolder);
        this.modules = modules;
    }

    @Override
    public String getProviderString() {
        return "terraform {\n" +
            "  required_providers {\n" +
            "    aws = {\n" +
            "      source  = \"hashicorp/aws\"\n" +
            "      version = \"~> 4.16\"\n" +
            "    }\n" +
            "  }\n" +
            "  required_version = \">= 1.2.0\"\n" +
            "}\n";
    }

    @Override
    public String getCredentialVariablesString() {
        HashSet<CloudProvider> cloudProviders = new HashSet<>();
        StringBuilder variables = new StringBuilder();
        for (TerraformModule module : modules) {
            CloudProvider cloudProvider = module.getCloudProvider();
            if (!cloudProviders.add(cloudProvider)) {
                continue;
            }
            if (cloudProvider.equals(CloudProvider.EDGE)) {
                variables.append(
                "variable \"edge_login_data\" {\n" +
                "  type = list(object({\n" +
                "    auth_user = string\n" +
                "    auth_pw = string\n" +
                "  }))\n" +
                "}\n"
                );
                continue;
            }

            String preFix = cloudProvider.toString().toLowerCase();
            variables.append(String.format(
                "variable \"%s_access_key\" {\n" +
                    "  type = string\n" +
                    "  default = \"\"\n" +
                    "}\n" +
                    "variable \"%s_secret_access_key\" {\n" +
                    "  type = string\n" +
                    "  default = \"\"\n" +
                    "}\n" +
                    "variable \"%s_session_token\" {\n" +
                    "  type = string\n" +
                    "  default = \"\"\n" +
                    "}\n", preFix, preFix, preFix));
        }
        return variables.toString();
    }

    public String getLocalModulesString() {
        StringBuilder moduleString = new StringBuilder();
        for (TerraformModule module : modules) {
            if (module.getCloudProvider().equals(CloudProvider.EDGE)) {
                moduleString.append(
                    "module \"edge\" {\n" +
                    "  source = \"./edge\"\n" +
                    "  login_data = var.edge_login_data\n" +
                    "}\n");
            } else {
                String moduleName = module.getModuleName();
                String prefix = module.getCloudProvider().toString().toLowerCase();
                moduleString.append(String.format(
                    "module \"%s\" {\n" +
                    "  source = \"./%s\"\n" +
                    "  access_key = var.%s_access_key\n" +
                    "  secret_access_key = var.%s_secret_access_key\n" +
                    "  session_token = var.%s_session_token\n" +
                    "}\n", moduleName, moduleName, prefix, prefix, prefix));
            }
        }
        return moduleString.toString();
    }

    @Override
    public String getOutputString() {
        StringBuilder functionsOutput = new StringBuilder(), vmOutput = new StringBuilder();
        String edgeOutput = "";
        for (TerraformModule module : modules) {
            functionsOutput.append(module.getFunctionsString());
            vmOutput.append(module.getVMString());
            edgeOutput = module.getEdgeString();
        }
        if (edgeOutput.isBlank()) {
            edgeOutput = "{}";
        }

        return String.format(
            "output \"function_urls\" {\n" +
            "   value = merge(%s)\n" +
            "}\n" +
            "output \"vm_urls\" {\n" +
            "  value = merge(%s)\n" +
            "}\n" +
            "output \"edge_urls\" {\n" +
            "  value = %s\n" +
            "}\n", functionsOutput, vmOutput, edgeOutput
        );
    }

    @Override
    protected String getMainFileContent() {
        return this.getProviderString() + this.getLocalModulesString();
    }

    @Override
    protected String getVariablesFileContent() {
        return this.getCredentialVariablesString();
    }

    @Override
    protected String getOutputsFileContent() {
        return this.getOutputString();
    }
}
