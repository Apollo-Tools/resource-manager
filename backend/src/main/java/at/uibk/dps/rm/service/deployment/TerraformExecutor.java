package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.model.Credentials;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TerraformExecutor {

    private final List<Credentials> credentials;

    private final String edgeLogin;

    public TerraformExecutor(List<Credentials> credentials, String edgeLogin) {
        this.credentials = credentials;
        this.edgeLogin = edgeLogin;
    }

    // TODO: test if this works on linux as well
    public void setPluginCacheFolder(Path folder) throws IOException, InterruptedException {
        Files.createDirectories(folder);
        String tfConfigContent = "plugin_cache_dir   = \"" + folder.toString().replace("\\", "/") + "\"";
        Path tfConfigPath;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            tfConfigPath = Paths.get(System.getenv("APPDATA") + "\\terraform.rc");
        } else {
            tfConfigPath = Paths.get(System.getenv("user.home") + "\\.terraformrc");
        }
        Files.writeString(tfConfigPath, tfConfigContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    public int init(Path folder) throws IOException, InterruptedException {
        ProcessExecutor processExecutor = new ProcessExecutor(folder, "terraform",  "init");
        return processExecutor.executeCli();
    }

    public int apply(Path folder)
        throws IOException, InterruptedException {
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
