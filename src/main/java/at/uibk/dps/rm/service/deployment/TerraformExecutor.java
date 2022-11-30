package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.CloudProvider;
import at.uibk.dps.rm.entity.deployment.Credentials;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TerraformExecutor {

    private final Map<CloudProvider, Credentials> credentialsMap = new HashMap<>();

    public void addCredentials(CloudProvider cloudProvider, Credentials credentials) {
        credentialsMap.put(cloudProvider, credentials);
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
        ProcessBuilder builder = new ProcessBuilder("terraform",  "-chdir=" + folder, "init");
        return executeCli(builder);
    }

    public int apply(Path folder) throws IOException, InterruptedException {
        List<String> variables = getCredentialsCommands();
        List<String> commands = new ArrayList<>(List.of("terraform", "-chdir=" + folder, "apply", "-auto-approve"));
        commands.addAll(variables);
        ProcessBuilder builder = new ProcessBuilder(commands);
        return executeCli(builder);
    }

    public int destroy(Path folder) {

        return -1;
    }
    
    private List<String> getCredentialsCommands() {
        List<String> variables = new ArrayList<>();
        for (Map.Entry<CloudProvider, Credentials> entry : credentialsMap.entrySet()) {
            String key = entry.getKey().toString().toLowerCase();
            variables.add("-var=\"" + key + "_access_key=" + entry.getValue().getAccessKey() + "\"");
            variables.add("-var=\"" + key + "_secret_access_key=" + entry.getValue().getSecretAccessKey() + "\"");
            variables.add("-var=\"" + key + "_session_token=" + entry.getValue().getSessionToken() + "\"");
        }
        return variables;
    }

    private int executeCli(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        processBuilder.redirectErrorStream(true);
        final Process initTF = processBuilder.start();
        Thread thread = printTerraformOutput(initTF);
        thread.start();
        initTF.waitFor();
        initTF.destroy();
        return initTF.exitValue();
    }

    private Thread printTerraformOutput(Process process) {
        return new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            input.lines().forEach(System.out::println);
        });
    }
}
