package at.uibk.dps.rm;

import at.uibk.dps.rm.util.JsonMapperConfig;
import at.uibk.dps.rm.verticle.MainVerticle;
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
        vertx.deployVerticle(new MainVerticle()).blockingSubscribe();
    }
}
