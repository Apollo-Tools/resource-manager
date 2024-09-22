package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to instantiate objects that are different types of executors.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestExecutorProvider {
    public static TerraformExecutor createTerraformExecutor() {
        return new TerraformExecutor();
    }

    public static MainTerraformExecutor createTerraformExecutorAWSOpenFaas() {
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSOpenfaas();
        return new MainTerraformExecutor(deploymentCredentials);
    }

    public static MainTerraformExecutor createTerraformExecutorAWS() {
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWS();
        return new MainTerraformExecutor(deploymentCredentials);
    }

    public static List<String> tfCommandsWithCredsAWSOpenFaas(String mainCommand, List<String> targets) {
        List<String> commands = tfCommandsWithCredsAWS(mainCommand, targets);
        String varSeparator = "", stringSperator = "\"";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            varSeparator = "\"";
            stringSperator = "\\\"";
        }
        commands.set(commands.size() - 1,
            "-var="+ varSeparator + "openfaas_login_data={r1={auth_user=" + stringSperator + "user" + stringSperator +
            ", auth_pw=" + stringSperator + "pw" + stringSperator + "}}" + varSeparator);
        return commands;
    }

    public static List<String> tfCommandsWithCredsAWSOpenFaas(String mainCommand) {
        return tfCommandsWithCredsAWSOpenFaas(mainCommand, List.of());
    }

    public static List<String> tfCommandsWithCredsAWS(String mainCommand, List<String> targets) {
        List<String> commands = new ArrayList<>();
        commands.add("terraform");
        if (mainCommand.equals("-refresh-only")) {
            commands.add("apply");
        }
        commands.add(mainCommand);
        commands.add("-auto-approve");
        if (!targets.isEmpty()) {
            targets.forEach(target -> {
                commands.add("-target");
                commands.add(target);
            });
        }
        String varSeparator = "";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            varSeparator = "\"";
        }
        commands.add("-var=" + varSeparator + "aws_access_key=accesskey" + varSeparator);
        commands.add("-var=" + varSeparator + "aws_secret_access_key=secretaccesskey" + varSeparator);
        commands.add("-var=" + varSeparator + "aws_session_token=sessiontoken" + varSeparator);
        commands.add("");
        return commands;
    }

    public static List<String> tfCommandsWithCredsAWS(String mainCommand) {
        return tfCommandsWithCredsAWS(mainCommand, List.of());
    }
}
