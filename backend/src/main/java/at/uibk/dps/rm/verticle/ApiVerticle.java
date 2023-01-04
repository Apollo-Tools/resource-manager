package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.router.*;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.JWTAuthProvider;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.CorsHandler;
import io.vertx.rxjava3.ext.web.handler.JWTAuthHandler;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class ApiVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);
    private static final String API_PREFIX = "/api/*";

    private JWTAuthProvider jwtAuthProvider;

    @Override
    public Completable rxStart() {
        return RouterBuilder.rxCreate(vertx, "backend/src/main/resources/openapi/resource-manager.yaml")
            .flatMap(routerBuilder -> {
                Router router = initRouter(routerBuilder);

                return vertx.createHttpServer()
                    .requestHandler(router)
                    .rxListen(config().getInteger("api_port"));
            })
            .doOnSuccess(
                http -> logger.info("HTTP server started on port " + config().getInteger("api_port")))
            .doOnError(
                throwable -> logger.error("Error", throwable))
            .ignoreElement();
    }

    private Router initRouter(RouterBuilder routerBuilder) {
        Router globalRouter = Router.router(vertx);
        setupFailureHandler(globalRouter);
        setupSecurityHandler(routerBuilder);
        setupRoutes(routerBuilder);
        Router apiRouter = routerBuilder
            .createRouter();
        globalRouter.route(API_PREFIX)
            .handler(cors())
            .subRouter(apiRouter);
        return globalRouter;
    }

    private void setupRoutes(RouterBuilder routerBuilder) {
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        AccountRoute.init(routerBuilder, serviceProxyProvider, jwtAuthProvider);
        ResourceProviderRoute.init(routerBuilder, serviceProxyProvider);
        CredentialsRoute.init(routerBuilder, serviceProxyProvider);
        ResourceRoute.init(routerBuilder, serviceProxyProvider);
        ResourceTypeRoute.init(routerBuilder, serviceProxyProvider);
        ResourceMetricRoute.init(routerBuilder, serviceProxyProvider);
        MetricRoute.init(routerBuilder, serviceProxyProvider);
        ResourceDeploymentRoute.init(routerBuilder, serviceProxyProvider);
        ReservationRoute.init(routerBuilder, serviceProxyProvider);
    }

    private void setupSecurityHandler(RouterBuilder routerBuilder) {
        jwtAuthProvider = new JWTAuthProvider(vertx,config().getString("jwt_algorithm"),
            config().getString("jwt_secret"), config().getInteger("token_minutes_valid"));
        routerBuilder
            .securityHandler("bearerAuth")
            .bindBlocking(config -> JWTAuthHandler.create(jwtAuthProvider.getJwtAuth()));
    }

    private void setupFailureHandler(Router router) {
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
                .putHeader("Content-type", "text/plain; charset=utf-8")
                .setStatusCode(rc.statusCode())
                .end(message);
        });
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