package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import io.vertx.rxjava3.core.Vertx;

import java.util.ArrayList;
import java.util.List;

public class TestExecutorProvider {
    public static TerraformExecutor createTerraformExecutorAWSEdge(Vertx vertx) {
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        return new TerraformExecutor(vertx, deploymentCredentials);
    }

    public static TerraformExecutor createTerraformExecutorAWS(Vertx vertx) {
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWS();
        return new TerraformExecutor(vertx, deploymentCredentials);
    }

    public static List<String> tfCommandsWithCredsAWSEdge(String mainCommand) {
        List<String> commands = tfCommandsWithCredsAWS(mainCommand);
        commands.set(commands.size() - 1, "-var=\"edge_login_data=[{auth_user=\\\"user\\\",auth_pw=\\\"pw\\\"},]\"");
        return commands;
    }

    public static List<String> tfCommandsWithCredsAWS(String mainCommand) {
        List<String> commands = new ArrayList<>();
        commands.add("terraform");
        commands.add(mainCommand);
        commands.add("-auto-approve");
        commands.add("-var=\"aws_access_key=accesskey\"");
        commands.add("-var=\"aws_secret_access_key=secretaccesskey\"");
        commands.add("-var=\"aws_session_token=sessiontoken\"");
        commands.add("");
        return commands;
    }
}
