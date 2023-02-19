package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.FunctionResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class ModuleFileService extends TerraformFileService {

    public ModuleFileService(Path rootFolder) {
        super(rootFolder);
    }

    /*** Cloud role ***/
    protected abstract String getRoleString(String roleName);

    /*** Serverless functions ***/
    protected abstract String getFunctionLocalsString(List<FunctionResource> resources, long reservationId,
                                                      Path rootFolder) throws IOException;

    protected abstract String getFunctionsString();

    protected abstract String getFunctionUrlString();

    /*** Virtual machines ***/
    protected abstract String getVmModulesString(List<FunctionResource> functionResources);

    /*** Output variables ***/
    protected abstract void setModuleGlobalOutputString();
}
