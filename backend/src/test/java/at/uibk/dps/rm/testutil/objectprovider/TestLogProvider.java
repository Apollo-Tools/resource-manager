package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import lombok.experimental.UtilityClass;

/**
 * Utility class to instantiate objects that are linked to the log entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestLogProvider {

    public static Log createLog(long id) {
        Log log = new Log();
        log.setLogId(id);
        log.setLogValue("log");
        return log;
    }

    public static DeploymentLog createDeploymentLog(long id, Deployment deployment) {
        DeploymentLog deploymentLog = new DeploymentLog();
        deploymentLog.setDeploymentLogId(id);
        deploymentLog.setDeployment(deployment);
        deploymentLog.setLog(createLog(id));
        return deploymentLog;
    }
}
