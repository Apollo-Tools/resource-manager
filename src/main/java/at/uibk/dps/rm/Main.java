package at.uibk.dps.rm;

import at.uibk.dps.rm.util.JsonMapperConfig;
import at.uibk.dps.rm.verticle.MainVerticle;
import io.vertx.rxjava3.core.Vertx;


public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        JsonMapperConfig.configJsonMapper();
        vertx.deployVerticle(new MainVerticle()).blockingSubscribe();
    }
}
