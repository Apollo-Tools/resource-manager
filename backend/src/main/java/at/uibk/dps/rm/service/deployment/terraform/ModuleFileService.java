package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.service.deployment.TerraformModule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class ModuleFileService extends TerraformFileService {

    private final TerraformModule module;

    public ModuleFileService(Path rootFolder, TerraformModule module) {
        super(rootFolder);
        this.module = module;
    }

    /*** Serverless functions ***/
    protected abstract String getFunctionsModulString(List<FunctionResource> resources, long reservationId,
                                                      Path rootFolder) throws IOException;

    /*** Virtual machines ***/
    protected abstract String getVmModulesString(List<FunctionResource> functionResources);

    /*** Output variables ***/
    protected abstract void setModuleResourceTypes();

    protected TerraformModule getModule() {
        return this.module;
    }
}
