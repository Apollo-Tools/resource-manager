package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.TerraformModule;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;

/**
 * Extends the #TerraformFileService for modules that deploy resource at cloud providers.
 *
 * @author matthi-g
 */
public abstract class ModuleFileService extends TerraformFileService {

    private final TerraformModule module;

    /**
     * Create an instance from the fileSystem, rootFolder and module.
     *
     * @param fileSystem the vertx file system
     * @param rootFolder the root folder of the module
     * @param module the terraform module
     */
    public ModuleFileService(FileSystem fileSystem, Path rootFolder, TerraformModule module) {
        super(fileSystem, rootFolder);
        this.module = module;
    }

    //*** Serverless functions ***//
    /**
     * Get functions string that defines all serverless functions and necessary additional resources
     * that have to be deployed.
     *
     * @return the functions string
     */
    protected abstract String getFunctionsModulString();

    //*** Virtual machines ***//
    /**
     * Get vm string that defines all virtual machines and necessary additional resources
     * that have to be deployed.
     *
     * @return the vm string
     */
    protected abstract String getVmModulesString();

    //*** Output variables ***//
    /**
     * Set the appearing resource types in the TerraformModule.
     */
    protected abstract void setModuleResourceTypes();

    protected TerraformModule getModule() {
        return this.module;
    }
}
