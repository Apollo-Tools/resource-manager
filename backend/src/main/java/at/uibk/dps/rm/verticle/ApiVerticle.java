package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.router.account.AccountRoute;
import at.uibk.dps.rm.router.account.CredentialsRoute;
import at.uibk.dps.rm.router.function.*;
import at.uibk.dps.rm.router.log.ReservationLogRoute;
import at.uibk.dps.rm.router.metric.MetricRoute;
import at.uibk.dps.rm.router.metric.ResourceMetricRoute;
import at.uibk.dps.rm.router.metric.ResourceTypeMetricRoute;
import at.uibk.dps.rm.router.reservation.ReservationRoute;
import at.uibk.dps.rm.router.resource.ResourceRoute;
import at.uibk.dps.rm.router.resource.ResourceTypeRoute;
import at.uibk.dps.rm.router.resourceprovider.RegionRoute;
import at.uibk.dps.rm.router.resourceprovider.ResourceProviderRegionRoute;
import at.uibk.dps.rm.router.resourceprovider.ResourceProviderRoute;
import at.uibk.dps.rm.router.resourceprovider.VPCRoute;
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

    private static final Logger logger = LoggerFactory.getLogger(ApiVerticle.class);
    private static final String API_PREFIX = "/api/*";

    private JWTAuthProvider jwtAuthProvider;

    @Override
    public Completable rxStart() {
        return RouterBuilder.rxCreate(vertx, "openapi/resource-manager.yaml")
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
        new AccountRoute().init(routerBuilder, serviceProxyProvider, jwtAuthProvider);
        new ResourceProviderRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceProviderRegionRoute().init(routerBuilder, serviceProxyProvider);
        new RegionRoute().init(routerBuilder, serviceProxyProvider);
        new CredentialsRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceTypeRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceMetricRoute().init(routerBuilder, serviceProxyProvider);
        new MetricRoute().init(routerBuilder, serviceProxyProvider);
        new RuntimeRoute().init(routerBuilder, serviceProxyProvider);
        new RuntimeTemplateRoute().init(routerBuilder, serviceProxyProvider);
        new FunctionRoute().init(routerBuilder, serviceProxyProvider);
        new FunctionResourceRoute().init(routerBuilder, serviceProxyProvider);
        new FunctionResourceSLORoute().init(routerBuilder, serviceProxyProvider);
        new ReservationRoute().init(routerBuilder, serviceProxyProvider);
        new ReservationLogRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceTypeMetricRoute().init(routerBuilder, serviceProxyProvider);
        new VPCRoute().init(routerBuilder, serviceProxyProvider);
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
            String message;
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
                .end();
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
