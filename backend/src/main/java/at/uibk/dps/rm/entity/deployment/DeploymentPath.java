package at.uibk.dps.rm.entity.deployment;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class DeploymentPath {

    private final Path rootFolder;

    private final Path functionsFolder;

    public DeploymentPath(long reservationId) {
        this.rootFolder = Path.of("var", "lib", "docker", "apollo", "temp","reservation_" + reservationId);
        this.functionsFolder = Path.of(rootFolder.toString(), "functions");
    }

    public Path getModuleFolder(TerraformModule module) {
        return Path.of(rootFolder.toString(), module.getModuleName());
    }

    public Path getTFCacheFolder() {
        return Path.of("data", "rm", "temp","plugin_cache").toAbsolutePath();
    }
}
