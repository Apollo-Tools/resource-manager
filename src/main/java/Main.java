import at.uibk.dps.rm.verticle.MainVerticle;
import io.vertx.mutiny.core.Vertx;


public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticleAndAwait(new MainVerticle());
    }
}
