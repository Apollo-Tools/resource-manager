package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.model.Credentials;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.file.FileSystem;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TerraformExecutor {

    private final List<Credentials> credentials;

    private final String edgeLogin;

    public TerraformExecutor(List<Credentials> credentials, String edgeLogin) {
        this.credentials = credentials;
        this.edgeLogin = edgeLogin;
    }

    // TODO: test if this works on linux as well
    public Completable setPluginCacheFolder(FileSystem fileSystem, Path folder) {
        String tfConfigContent = "plugin_cache_dir   = \"" + folder.toString().replace("\\", "/") + "\"";
        Path tfConfigPath;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            tfConfigPath = Paths.get(System.getenv("APPDATA") + "\\terraform.rc");
        } else {
            tfConfigPath = Paths.get(System.getenv("user.home") + "\\.terraformrc");
        }
        return fileSystem.mkdirs(folder.toString())
            .andThen(fileSystem.writeFile(tfConfigPath.toString(), Buffer.buffer(tfConfigContent)));
    }

    public Single<Process> init(Path folder) throws IOException, InterruptedException {
        ProcessExecutor processExecutor = new ProcessExecutor(folder, "terraform",  "init");
        return processExecutor.executeCli();
    }

    public Single<Process> apply(Path folder)
        throws IOException {
        List<String> variables = getCredentialsCommands();
        List<String> commands = new ArrayList<>(List.of("terraform", "apply", "-auto-approve"));
        commands.addAll(variables);
        commands.add(getEdgeLoginCommand());
        ProcessExecutor processExecutor = new ProcessExecutor(folder, commands);
        return processExecutor.executeCli();
    }

    public int destroy(Path folder) {
        // TODO: implemnt
        return -1;
    }
    
    private List<String> getCredentialsCommands() {
        List<String> variables = new ArrayList<>();
        for (Credentials entry : credentials) {
            String prefix = entry.getResourceProvider().getProvider().toLowerCase();
            variables.add("-var=\"" + prefix + "_access_key=" + entry.getAccessKey() + "\"");
            variables.add("-var=\"" + prefix + "_secret_access_key=" + entry.getSecretAccessKey() + "\"");
            variables.add("-var=\"" + prefix + "_session_token=" + entry.getSessionToken() + "\"");
        }
        return variables;
    }

    private String getEdgeLoginCommand() {
        if (edgeLogin.isBlank()) {
            return "";
        }
        return "-var=\"" + edgeLogin + "\"";
    }
}
