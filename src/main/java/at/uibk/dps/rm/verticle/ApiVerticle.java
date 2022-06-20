package at.uibk.dps.rm.verticle;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.eventbus.EventBus;

public class ApiVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

    @Override
    public Completable rxStart() {
        EventBus eb = vertx.eventBus();
        return vertx.createHttpServer()
                .requestHandler(req -> {
                    // TODO: Introduce Service Proxy
                    eb.send("insert-metric", "");
                    req.response().putHeader("content-type", "text/plain")
                            .end("Success");
                })
                .rxListen(config().getInteger("api_port"))
                .doOnSuccess(http -> logger.info("HTTP server started on port " + config().getInteger("api_port")))
                .doOnError(throwable -> logger.error("Error", throwable))
                .ignoreElement();
    }
}
