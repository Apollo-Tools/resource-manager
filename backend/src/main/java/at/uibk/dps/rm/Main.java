package at.uibk.dps.rm;

import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import at.uibk.dps.rm.verticle.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.rxjava3.core.Vertx;
import lombok.experimental.UtilityClass;

/**
 * The main entry class of the Resource Manager
 *
 * @author matthi-g
 */
@UtilityClass
public class Main {

    /**
     * The main method of the Resource Manager
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JsonMapperConfig.configJsonMapper();
        Vertx vertx = Vertx.vertx();
        new ConfigUtility(vertx).getConfigJson().flatMapCompletable(config -> vertx
            .deployVerticle(new MainVerticle(), new DeploymentOptions().setConfig(config)).ignoreElement())
            .blockingSubscribe();
    }
}
