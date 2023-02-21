package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.service.deployment.TerraformModule;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

public class MainFileService extends TerraformFileService {

    private final List<TerraformModule> modules;

    public MainFileService(Path rootFolder, List<TerraformModule> modules) {
        super(rootFolder);
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
            "    time = {\n" +
            "      source = \"hashicorp/time\"\n" +
            "      version = \"0.9.1\"\n" +
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
            if (cloudProviders.contains(cloudProvider)) {
                continue;
            } else {
                cloudProviders.add(cloudProvider);
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
        return moduleString.toString();
    }

    // TODO: add edge output
    @Override
    public String getOutputString() {
        StringBuilder functionsOutput = new StringBuilder(), vmOutput = new StringBuilder(),
            edgeOutput = new StringBuilder();
        for (TerraformModule module : modules) {
            functionsOutput.append(module.getFunctionsString());
            vmOutput.append(module.getVMString());
            edgeOutput.append(module.getEdgeString());
        }
        return String.format(
            "output \"function_urls\" {\n" +
            "   value = merge(%s)\n" +
            "}\n" +
            "output \"vm_urls\" {\n" +
            "  value = merge(%s)\n" +
            "}\n", functionsOutput, vmOutput
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
