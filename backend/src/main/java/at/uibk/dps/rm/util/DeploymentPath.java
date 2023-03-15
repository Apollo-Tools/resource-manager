package at.uibk.dps.rm.util;

import at.uibk.dps.rm.service.deployment.TerraformModule;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class DeploymentPath {

    private final Path rootFolder;

    private final Path functionsFolder;

    public DeploymentPath(long reservationId) {
        this.rootFolder = Paths.get("temp\\reservation_" + reservationId);
        this.functionsFolder = Path.of(rootFolder.toString(), "functions");
    }

    public Path getModuleFolder(TerraformModule module) {
        return Paths.get(rootFolder + "\\" + module.getModuleName());
    }

    public Path getTFCacheFolder() {
        return Paths.get("temp\\plugin_cache").toAbsolutePath();
    }
}
