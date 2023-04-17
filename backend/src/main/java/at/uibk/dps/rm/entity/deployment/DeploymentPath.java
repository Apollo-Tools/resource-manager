package at.uibk.dps.rm.entity.deployment;

import io.vertx.core.json.JsonObject;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class DeploymentPath {

    private final Path rootFolder;

    private final Path functionsFolder;

    private final Path buildFolder;

    public DeploymentPath(long reservationId, JsonObject config) {
        this.buildFolder = Path.of(config.getString("build_directory"));
        this.rootFolder = Path.of(buildFolder.toString(), "reservation_" + reservationId);
        this.functionsFolder = Path.of(rootFolder.toString(), "functions");
    }

    public Path getModuleFolder(TerraformModule module) {
        return Path.of(rootFolder.toString(), module.getModuleName());
    }

    public Path getTFCacheFolder() {
        return Path.of(buildFolder.toString(), "plugin_cache").toAbsolutePath();
    }
}
