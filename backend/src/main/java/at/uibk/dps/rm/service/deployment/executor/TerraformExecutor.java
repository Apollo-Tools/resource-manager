package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import io.reactivex.rxjava3.core.Single;
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

    /**
     * Execute the terraform init operation in the folder path.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the init operation
     */
    public Single<ProcessOutput> init(Path folder) {
        ProcessExecutor processExecutor = new ProcessExecutor(folder, List.of("terraform",  "init"));
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
     * Execute the terraform apply operation in the folder with resource targeting.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the apply operation
     */
    public Single<ProcessOutput> apply(Path folder, List<String> targets) {
        List<String> commands = new ArrayList<>(List.of("terraform", "apply", "-auto-approve"));
        targets.forEach(target -> {
            commands.add("-target");
            commands.add(target);
        });
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    /**
     * Execute the terraform refresh operation to refresh the state.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the apply operation
     */
    public Single<ProcessOutput> refresh(Path folder) {
        List<String> commands = new ArrayList<>(List.of("terraform", "refresh"));
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

    /**
     * Execute the terraform destroy operation in the folder with resource targeting.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the destroy operation
     */
    public Single<ProcessOutput> destroy(Path folder, List<String> targets) {
        List<String> commands = new ArrayList<>(List.of("terraform", "destroy", "-auto-approve"));
        targets.forEach(target -> {
            commands.add("-target");
            commands.add(target);
        });
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }
}
