package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.service.deployment.executor.MainTerraformExecutor;
import io.vertx.rxjava3.core.Vertx;
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
    public static MainTerraformExecutor createTerraformExecutorAWSEdge(Vertx vertx) {
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWSEdge();
        return new MainTerraformExecutor(vertx, deploymentCredentials);
    }

    public static MainTerraformExecutor createTerraformExecutorAWS(Vertx vertx) {
        DeploymentCredentials deploymentCredentials = TestDTOProvider.createDeploymentCredentialsAWS();
        return new MainTerraformExecutor(vertx, deploymentCredentials);
    }

    public static List<String> tfCommandsWithCredsAWSEdge(String mainCommand) {
        List<String> commands = tfCommandsWithCredsAWS(mainCommand);
        String varSeparator = "", stringSperator = "\"";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            varSeparator = "\"";
            stringSperator = "\\\"";
        }
        commands.set(commands.size() - 1, "-var="+ varSeparator + "edge_login_data=[{auth_user=" + stringSperator +
            "user" + stringSperator +",auth_pw=" + stringSperator + "pw" + stringSperator + "}," + "]" + varSeparator);
        return commands;
    }

    public static List<String> tfCommandsWithCredsAWS(String mainCommand) {
        List<String> commands = new ArrayList<>();
        commands.add("terraform");
        commands.add(mainCommand);
        commands.add("-auto-approve");
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
}
