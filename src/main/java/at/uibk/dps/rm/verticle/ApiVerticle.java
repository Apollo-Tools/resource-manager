package at.uibk.dps.rm.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

public class ApiVerticle extends AbstractVerticle {

    @Override public void start(Promise<Void> startPromise){
        EventBus eb = vertx.eventBus();

        vertx.createHttpServer().requestHandler(req -> {
            // Send message to event bus
            // TODO: Introduce Service Proxy
            eb.send("insert-metric","");
            req.response()
                .putHeader("content-type", "text/plain")
                .end("Hello from Vert.x!");
        }).listen(config().getInteger("api_port"), http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port " + config().getInteger("api_port"));
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
