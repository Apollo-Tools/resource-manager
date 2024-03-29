package at.uibk.dps.rm.entity.deployment;

import at.uibk.dps.rm.entity.deployment.module.TerraformModule;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import lombok.Getter;

import java.nio.file.Path;

/**
 * Class that is used to indicate where files generated during the deployment process have to be
 * stored.
 *
 * @author matthi-g
 */
@Getter
public class DeploymentPath {

    private final Path rootFolder;

    private final Path functionsFolder;

    private final Path layersFolder;

    private final Path templatesFolder;

    private final Path buildFolder;

    /**
     * Create a new instance from the deploymentId and vertx config.
     *
     * @param deploymentId the id of the deployment
     * @param config the vertx config
     */
    public DeploymentPath(long deploymentId, ConfigDTO config) {
        this.buildFolder = Path.of(config.getBuildDirectory());
        this.rootFolder = Path.of(buildFolder.toString(), "deployment_" + deploymentId);
        this.functionsFolder = Path.of(rootFolder.toString(), "functions");
        this.layersFolder = Path.of(functionsFolder.toString(), "layers");
        this.templatesFolder = Path.of(functionsFolder.toString(), "template");
    }

    /**
     * Get the folder where all deployment files are stored for a module.
     *
     * @param module the concerning module
     * @return the path to the module folder
     */
    public Path getModuleFolder(TerraformModule module) {
        return Path.of(rootFolder.toString(), module.getModuleName());
    }
}
