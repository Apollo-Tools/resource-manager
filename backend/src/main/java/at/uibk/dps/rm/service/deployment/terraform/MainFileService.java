package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.TerraformModule;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

/**
 * Extension of the #TerraformFileService to set up the main module of a deployment.
 *
 * @author matthi-g
 */
public class MainFileService extends TerraformFileService {

    private final List<TerraformModule> modules;

    /**
     * Create an instance from the fileSystem, rootFolder and modules.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param modules the list of sub modules
     */
    public MainFileService(FileSystem fileSystem, Path rootFolder, List<TerraformModule> modules) {
        super(fileSystem, rootFolder);
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
            "    kubernetes = {\n" +
            "      source = \"hashicorp/kubernetes\"\n" +
            "      version = \"2.20.0\"\n" +
            "    }\n" +
            "  }\n" +
            "  required_version = \">= 1.2.0\"\n" +
            "}\n";
    }

    /**
     * Get the string that defines all submodules of the deployment.
     *
     * @return the definition of all submodules
     */
    public String getLocalModulesString() {
        StringBuilder moduleString = new StringBuilder();
        for (TerraformModule module : modules) {
            if (module.getCloudProvider().equals(CloudProvider.EDGE)) {
                moduleString.append(
                    "module \"edge\" {\n" +
                    "  source = \"./edge\"\n" +
                    "  login_data = var.edge_login_data\n" +
                    "}\n");
            } else if (module.getCloudProvider().equals(CloudProvider.AWS)){
                String moduleName = module.getModuleName();
                String prefix = module.getCloudProvider().toString().toLowerCase();
                moduleString.append(String.format(
                    "module \"%s\" {\n" +
                    "  source = \"./%s\"\n" +
                    "  access_key = var.%s_access_key\n" +
                    "  secret_access_key = var.%s_secret_access_key\n" +
                    "  session_token = var.%s_session_token\n" +
                    "  openfaas_login_data = var.openfaas_login_data\n" +
                    "}\n", moduleName, moduleName, prefix, prefix, prefix));
            } else {
                moduleString.append(
                    "module \"container\" {\n" +
                    "  source = \"./container\"\n" +
                    "}\n");
            }
        }
        return moduleString.toString();
    }

    @Override
    protected String getMainFileContent() {
        return this.getProviderString() + this.getLocalModulesString();
    }

    @Override
    protected String getVariablesFileContent() {
        HashSet<CloudProvider> cloudProviders = new HashSet<>();
        StringBuilder variables = new StringBuilder();
        for (TerraformModule module : modules) {
            CloudProvider cloudProvider = module.getCloudProvider();
            if (!cloudProviders.add(cloudProvider)) {
                continue;
            }
            if (cloudProvider.equals(CloudProvider.EDGE)) {
                variables.append(
                    "variable openfaas_login_data {\n" +
                        "  type = list(map(object({\n" +
                        "      auth_user = string\n" +
                        "      auth_pw = string\n" +
                        "  })))\n" +
                        "}"
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
        variables.append(
            "variable \"openfaas_login_data\" {\n" +
                "  type = map(object({\n" +
                "      auth_user = string\n" +
                "      auth_pw = string\n" +
                "  }))\n" +
                "  default = {}\n" +
                "}\n"
        );

        return variables.toString();
    }

    @Override
    protected String getOutputsFileContent() {
        StringBuilder functionsOutput = new StringBuilder();
        for (TerraformModule module : modules) {
            if (module.getHasFaas()) {
                functionsOutput.append(module.getFunctionsString());
            }
        }

        return String.format(
            "output \"function_urls\" {\n" +
                "   value = merge(%s)\n" +
                "}\n", functionsOutput
        );
    }
}
