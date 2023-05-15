package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.*;

/**
 * Provides the implementation of different terraform operations.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class TerraformExecutor {

    private final Vertx vertx;

    /**
     * Set the folder where terraform should cache its data. By doing this terraform can reuse
     * already downloaded providers.
     *
     * @param folder the path to the cache directory
     * @return a Completable
     */
    public Completable setPluginCacheFolder(Path folder) {
        String tfConfigContent = "plugin_cache_dir = \"" + Path.of(folder.toString())
            .toAbsolutePath().toString().replace("\\", "/") + "\"";
        Path tfConfigPath = Path.of("terraform", "config.tfrc");
        return vertx.fileSystem().mkdirs(folder.toString())
            .andThen(vertx.fileSystem().writeFile(tfConfigPath.toString(), Buffer.buffer(tfConfigContent)));
    }

    /**
     * Execute the terraform init operation in the folder path.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the init operation
     */
    public Single<ProcessOutput> init(Path folder) {
        ProcessExecutor processExecutor = new ProcessExecutor(folder, "terraform",  "init");
        return processExecutor.executeCli();
    }

    /**
     * Execute the terraform apply operation in the folder.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the apply operation
     */
    public Single<ProcessOutput> apply(Path folder) {
        List<String> commands = new ArrayList<>(List.of("terraform", "apply", "-auto-approve"));
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    /**
     * Execute the terraform output operation in the folder. The output of this operation is
     * formatted as JSON
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the output operation
     */
    public Single<ProcessOutput> getOutput(Path folder) {
        List<String> commands = new ArrayList<>(List.of("terraform", "output", "--json"));
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    /**
     * Execute the terraform destroy operation in the folder.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the destroy operation
     */
    public Single<ProcessOutput> destroy(Path folder) {
        List<String> commands = new ArrayList<>(List.of("terraform", "destroy", "-auto-approve"));
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }
}
