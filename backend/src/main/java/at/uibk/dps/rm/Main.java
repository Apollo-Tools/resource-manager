package at.uibk.dps.rm;

import at.uibk.dps.rm.util.JsonMapperConfig;
import at.uibk.dps.rm.verticle.MainVerticle;
import io.vertx.rxjava3.core.Vertx;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Main {
    public static void main(final String[] args) {
        JsonMapperConfig.configJsonMapper();
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle()).blockingSubscribe();
    }
}
