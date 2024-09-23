package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.model.Credentials;
import io.reactivex.rxjava3.core.Single;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends the default terraform executor with additional parameters
 * for used for the deployment of the main modules.
 *
 * @author matthi-g
 */
public class MainTerraformExecutor extends TerraformExecutor {

    private final DeploymentCredentials credentials;

    /**
     * Create an instance from vertx and credentials
     *
     * @param credentials the deployment credentials
     */
    public MainTerraformExecutor(DeploymentCredentials credentials) {
        super();
        this.credentials = credentials;
    }

    @Override
    public Single<ProcessOutput> apply(Path folder) {
        return apply(folder, List.of());
    }

    @Override
    public Single<ProcessOutput> apply(Path folder, List<String> targets) {
        List<String> commands = new ArrayList<>(List.of("terraform", "apply", "-auto-approve"));
        targets.forEach(target -> {
            commands.add("-target");
            commands.add(target);
        });
        return executeCliWithCredentials(folder, commands);
    }

    /**
     * Execute the terraform destroy operation in the folder.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the destroy operation
     */
    @Override
    public Single<ProcessOutput> destroy(Path folder) {
        return destroy(folder, List.of());
    }

    @Override
    public Single<ProcessOutput> destroy(Path folder, List<String> targets) {
        List<String> commands = new ArrayList<>(List.of("terraform", "destroy", "-auto-approve"));
        targets.forEach(target -> {
            commands.add("-target");
            commands.add(target);
        });
        return executeCliWithCredentials(folder, commands);
    }

    @Override
    public Single<ProcessOutput> refresh(Path folder) {
        List<String> commands = new ArrayList<>(List.of("terraform", "apply", "-refresh-only", "-auto-approve"));
        return executeCliWithCredentials(folder, commands);
    }

    /**
     * Execute the terraform cli with credentials in the provided folder with the given commands.
     *
     * @param folder the folder path
     * @param commands the commands to execute
     * @return a Single that emits the process output of the cli operation
     */
    protected Single<ProcessOutput> executeCliWithCredentials(Path folder, List<String> commands) {
        List<String> cloudCredentials = getCloudCredentialsCommands();
        commands.addAll(cloudCredentials);
        commands.add(getOpenFaasCredentialsCommand());
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    /**
     * Get the cloud credentials that are necessary for the deployment.
     *
     * @return a List of cloud credentials formatted as terraform variables
     */
    protected List<String> getCloudCredentialsCommands() {
        List<String> variables = new ArrayList<>();
        String separator = getOsVariableSeparator();
        for (Credentials entry : credentials.getCloudCredentials()) {
            String prefix = entry.getResourceProvider().getProvider().toLowerCase();
            variables.add("-var="  + separator + prefix + "_access_key=" + entry.getAccessKey() + separator);
            variables.add("-var="  + separator + prefix + "_secret_access_key=" + entry.getSecretAccessKey() +
                separator);
            if (entry.getSessionToken() != null) {
                variables.add("-var="  + separator + prefix + "_session_token=" + entry.getSessionToken() + separator);
            }
        }
        return variables;
    }

    /**
     * Get the edge credentials that are necessary for the deployment.
     *
     * @return the edge credentials formatted as terraform variables
     */
    protected String getOpenFaasCredentialsCommand() {
        String openFaasCredentials = credentials.getOpenFaasCredentialsString();
        if (openFaasCredentials.isBlank()) {
            return "";
        }
        String separator = getOsVariableSeparator();
        return "-var=" + separator + openFaasCredentials + separator;
    }

    /**
     * Get the variable separator dependent on the current operating system.
     *
     * @return the variable separator
     */
    private String getOsVariableSeparator() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return "\"";
        } else {
            return "";
        }
    }
}
