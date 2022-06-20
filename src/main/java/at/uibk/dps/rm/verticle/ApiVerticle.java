package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.router.ResourceTypeRouter;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.CorsHandler;

public class ApiVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

    @Override
    public Completable rxStart() {
       Router router = Router.router(vertx);

        router.route()
            .handler(cors())
            .handler(BodyHandler.create());

        router.route("/api/resource-types*").subRouter(ResourceTypeRouter.router(vertx));

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(config().getInteger("api_port"))
                .doOnSuccess(http -> logger.info("HTTP server started on port " + config().getInteger("api_port")))
                .doOnError(throwable -> logger.error("Error", throwable))
                .ignoreElement();
    }

    private CorsHandler cors() {
        return CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.PATCH)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Authorization")
                .allowedHeader("Content-Type");
    }

}
