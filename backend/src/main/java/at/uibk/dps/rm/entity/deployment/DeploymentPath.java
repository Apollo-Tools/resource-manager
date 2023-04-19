package at.uibk.dps.rm.entity.deployment;

import io.vertx.core.json.JsonObject;
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

    private final Path buildFolder;

    /**
     * Create a new instance from the reservationId and vertx config.
     *
     * @param reservationId the id of the reservation
     * @param config the vertx config
     */
    public DeploymentPath(final long reservationId, final JsonObject config) {
        this.buildFolder = Path.of(config.getString("build_directory"));
        this.rootFolder = Path.of(buildFolder.toString(), "reservation_" + reservationId);
        this.functionsFolder = Path.of(rootFolder.toString(), "functions");
    }

    /**
     * Get the folder where all deployment files are stored for a module.
     *
     * @param module the concerning module
     * @return the path to the module folder
     */
    public Path getModuleFolder(final TerraformModule module) {
        return Path.of(rootFolder.toString(), module.getModuleName());
    }

    /**
     * Get the path of the terraform cache.
     *
     * @return the terraform cache
     */
    public Path getTFCacheFolder() {
        return Path.of(buildFolder.toString(), "plugin_cache").toAbsolutePath();
    }
}
