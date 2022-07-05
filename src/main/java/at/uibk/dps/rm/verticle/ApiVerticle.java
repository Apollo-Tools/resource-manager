package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.router.MetricRouter;
import at.uibk.dps.rm.router.ResourceRouter;
import at.uibk.dps.rm.router.ResourceTypeRouter;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.CorsHandler;
import io.vertx.rxjava3.ext.web.handler.ResponseContentTypeHandler;

public class ApiVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);
    private static final String API_PREFIX = "/api/";

    @Override
    public Completable rxStart() {
       Router router = Router.router(vertx);

        router.route()
            .handler(cors())
            .handler(BodyHandler.create())
            .handler(ResponseContentTypeHandler.create());

        router.route(API_PREFIX + "resource-types*").subRouter(ResourceTypeRouter.router(vertx));
        router.route(API_PREFIX + "resources*").subRouter(ResourceRouter.router(vertx));
        router.route(API_PREFIX + "metrics*").subRouter(MetricRouter.router(vertx));

        router.route().failureHandler(rc -> {
            String message = "";
            if (rc.statusCode() == 500) {
                message = "internal server error";
                logger.error(message, rc.failure());
            } else if (rc.failure() != null){
                message = rc.failure().getMessage();
                logger.warn("Code " + rc.statusCode() + ": " + message);
            }

            rc.response()
                .putHeader("Content-type", "application/json; charset=utf-8")
                .setStatusCode(rc.statusCode())
                .end(message);
        });

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
