package at.uibk.dps.rm.service.deployment.executor;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.model.Credentials;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TerraformExecutor {

    private final Vertx vertx;

    private final DeploymentCredentials credentials;

    public TerraformExecutor(Vertx vertx, DeploymentCredentials deploymentCredentials) {
        this.vertx = vertx;
        this.credentials = deploymentCredentials;
    }


    // TODO: test if this works on linux as well
    public Completable setPluginCacheFolder(Path folder) {
        String tfConfigContent = "plugin_cache_dir   = \"" + folder.toString().replace("\\", "/") + "\"";
        Path tfConfigPath;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            tfConfigPath = Paths.get(System.getenv("APPDATA") + "\\terraform.rc");
        } else {
            tfConfigPath = Paths.get(System.getenv("user.home") + "\\.terraformrc");
        }
        return vertx.fileSystem().mkdirs(folder.toString())
            .andThen(vertx.fileSystem().writeFile(tfConfigPath.toString(), Buffer.buffer(tfConfigContent)));
    }

    public Single<ProcessOutput> init(Path folder) throws IOException, InterruptedException {
        ProcessExecutor processExecutor = new ProcessExecutor(folder, "terraform",  "init");
        return processExecutor.executeCli();
    }

    public Single<ProcessOutput> apply(Path folder) {
        List<String> variables = getCredentialsCommands();
        List<String> commands = new ArrayList<>(List.of("terraform", "apply", "-auto-approve"));
        commands.addAll(variables);
        commands.add(getEdgeLoginCommand());
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    public Single<ProcessOutput> getOutput(Path folder) {
        List<String> commands = new ArrayList<>(List.of("terraform", "output", "--json"));
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    public int destroy(Path folder) {
        // TODO: implement
        return -1;
    }
    
    private List<String> getCredentialsCommands() {
        List<String> variables = new ArrayList<>();
        for (Credentials entry : credentials.getCloudCredentials()) {
            String prefix = entry.getResourceProvider().getProvider().toLowerCase();
            variables.add("-var=\"" + prefix + "_access_key=" + entry.getAccessKey() + "\"");
            variables.add("-var=\"" + prefix + "_secret_access_key=" + entry.getSecretAccessKey() + "\"");
            variables.add("-var=\"" + prefix + "_session_token=" + entry.getSessionToken() + "\"");
        }
        return variables;
    }

    private String getEdgeLoginCommand() {
        String edgeLogin = credentials.getEdgeLoginCredentials().toString();
        if (edgeLogin.isBlank()) {
            return "";
        }
        return "-var=\"" + edgeLogin + "\"";
    }
}