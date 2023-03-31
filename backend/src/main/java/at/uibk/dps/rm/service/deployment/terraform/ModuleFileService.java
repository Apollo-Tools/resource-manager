package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.TerraformModule;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.IOException;
import java.nio.file.Path;
public abstract class ModuleFileService extends TerraformFileService {

    private final TerraformModule module;

    public ModuleFileService(FileSystem fs, Path rootFolder, TerraformModule module) {
        super(fs, rootFolder);
        this.module = module;
    }

    /*** Serverless functions ***/
    protected abstract String getFunctionsModulString() throws IOException;

    /*** Virtual machines ***/
    protected abstract String getVmModulesString();

    /*** Output variables ***/
    protected abstract void setModuleResourceTypes();

    protected TerraformModule getModule() {
        return this.module;
    }
}
