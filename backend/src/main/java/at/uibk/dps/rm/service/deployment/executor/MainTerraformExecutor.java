package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.model.Credentials;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the implementation of different terraform operations.
 *
 * @author matthi-g
 */
public class MainTerraformExecutor extends TerraformExecutor{

    private final DeploymentCredentials credentials;

    /**
     * Create an instance from vertx and credentials
     *
     * @param vertx the vertx instance
     * @param credentials the deployment credentials
     */
    public MainTerraformExecutor(Vertx vertx, DeploymentCredentials credentials) {
        super(vertx);
        this.credentials = credentials;
    }

    @Override
    public Single<ProcessOutput> apply(Path folder) {
        List<String> cloudCredentials = getCloudCredentialsCommands();
        List<String> commands = new ArrayList<>(List.of("terraform", "apply", "-auto-approve"));
        commands.addAll(cloudCredentials);
        commands.add(getEdgeCredentialsCommand());
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    /**
     * Execute the terraform destroy operation in the folder.
     *
     * @param folder the folder path
     * @return a Single that emits the process output of the destroy operation
     */
    @Override
    public Single<ProcessOutput> destroy(Path folder) {
        List<String> cloudCredentials = getCloudCredentialsCommands();
        List<String> commands = new ArrayList<>(List.of("terraform", "destroy", "-auto-approve"));
        commands.addAll(cloudCredentials);
        commands.add(getEdgeCredentialsCommand());
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
            variables.add("-var="  + separator + prefix + "_session_token=" + entry.getSessionToken() + separator);
        }
        return variables;
    }

    /**
     * Get the edge credentials that are necessary for the deployment.
     *
     * @return the edge credentials formatted as terraform variables
     */
    protected String getEdgeCredentialsCommand() {
        String edgeLogin = credentials.getEdgeLoginCredentials();
        if (edgeLogin.isBlank()) {
            return "";
        }
        String separator = getOsVariableSeparator();
        return "-var=" + separator + edgeLogin + separator;
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
