package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.router.account.AccountNamespaceRoute;
import at.uibk.dps.rm.router.account.AccountRoute;
import at.uibk.dps.rm.router.account.CredentialsRoute;
import at.uibk.dps.rm.router.account.NamespaceRoute;
import at.uibk.dps.rm.router.artifact.FunctionTypeRoute;
import at.uibk.dps.rm.router.artifact.ServiceTypeRoute;
import at.uibk.dps.rm.router.ensemble.EnsembleRoute;
import at.uibk.dps.rm.router.ensemble.ResourceEnsembleRoute;
import at.uibk.dps.rm.router.function.*;
import at.uibk.dps.rm.router.log.DeploymentLogRoute;
import at.uibk.dps.rm.router.metric.MetricRoute;
import at.uibk.dps.rm.router.metric.ResourceMetricRoute;
import at.uibk.dps.rm.router.metric.PlatformMetricRoute;
import at.uibk.dps.rm.router.deployment.DeploymentRoute;
import at.uibk.dps.rm.router.deployment.ResourceDeploymentRoute;
import at.uibk.dps.rm.router.resource.*;
import at.uibk.dps.rm.router.resourceprovider.*;
import at.uibk.dps.rm.router.service.ServiceRoute;
import at.uibk.dps.rm.router.service.K8sServiceTypeRoute;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.configuration.JWTAuthProvider;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.CorsHandler;
import io.vertx.rxjava3.ext.web.handler.JWTAuthHandler;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Everything that relates to the api of the resource manager is executed on the ApiVerticle.
 *
 * @author matthi-g
 */
public class ApiVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ApiVerticle.class);
    private static final String API_PREFIX = "/api/*";

    private JWTAuthProvider jwtAuthProvider;

    @Override
    public Completable rxStart() {
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        return RouterBuilder.create(vertx, "openapi/resource-manager.yaml")
            .flatMap(routerBuilder -> {
                Router router = initRouter(routerBuilder, config);
                return vertx.createHttpServer()
                    .requestHandler(router)
                    .rxListen(config.getApiPort());
            })
            .doOnSuccess(
                http -> logger.info("HTTP server started on port " + config.getApiPort()))
            .doOnError(
                throwable -> logger.error("Error", throwable))
            .ignoreElement();
    }

    /**
     * Initialise the route for the api using the routerBuilder.
     *
     * @param routerBuilder the router builder
     * @param config the config
     * @return the initialised router
     */
    private Router initRouter(RouterBuilder routerBuilder, ConfigDTO config) {
        Router globalRouter = Router.router(vertx);
        setupBodyHandler(globalRouter, config);
        setupFailureHandler(globalRouter);
        setupSecurityHandler(routerBuilder, config);
        setupRoutes(routerBuilder);
        Router apiRouter = routerBuilder
            .createRouter();
        globalRouter.route(API_PREFIX)
            .handler(cors())
            .subRouter(apiRouter);

        return globalRouter;
    }

    /**
     * Setup all api routes.
     *
     * @param routerBuilder the router builder
     */
    private void setupRoutes(RouterBuilder routerBuilder) {
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        new AccountNamespaceRoute().init(routerBuilder, serviceProxyProvider);
        new AccountRoute().init(routerBuilder, serviceProxyProvider, jwtAuthProvider);
        new CredentialsRoute().init(routerBuilder, serviceProxyProvider);
        new DeploymentLogRoute().init(routerBuilder, serviceProxyProvider);
        new DeploymentRoute().init(routerBuilder, serviceProxyProvider);
        new EnsembleRoute().init(routerBuilder, serviceProxyProvider);
        new EnvironmentRoute().init(routerBuilder, serviceProxyProvider);
        new FunctionRoute().init(routerBuilder, serviceProxyProvider);
        new FunctionTypeRoute().init(routerBuilder, serviceProxyProvider);
        new K8sServiceTypeRoute().init(routerBuilder, serviceProxyProvider);
        new MetricRoute().init(routerBuilder, serviceProxyProvider);
        new NamespaceRoute().init(routerBuilder, serviceProxyProvider);
        new PlatformMetricRoute().init(routerBuilder, serviceProxyProvider);
        new PlatformRegionRoute().init(routerBuilder, serviceProxyProvider);
        new PlatformRoute().init(routerBuilder, serviceProxyProvider);
        new RegionRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceEnsembleRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceDeploymentRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceMetricRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceProviderRegionRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceProviderRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceRoute().init(routerBuilder, serviceProxyProvider);
        new ResourceTypeRoute().init(routerBuilder, serviceProxyProvider);
        new RuntimeRoute().init(routerBuilder, serviceProxyProvider);
        new RuntimeTemplateRoute().init(routerBuilder, serviceProxyProvider);
        new ServiceRoute().init(routerBuilder, serviceProxyProvider);
        new ServiceTypeRoute().init(routerBuilder, serviceProxyProvider);
        new VPCRoute().init(routerBuilder, serviceProxyProvider);
    }

    /**
     * Set up the security handler.
     *
     * @param routerBuilder the router builder
     */
    private void setupSecurityHandler(RouterBuilder routerBuilder, ConfigDTO configDTO) {
        jwtAuthProvider = new JWTAuthProvider(vertx, configDTO.getJwtAlgorithm(), configDTO.getJwtSecret(),
            configDTO.getTokenMinutesValid());
        routerBuilder
            .securityHandler("bearerAuth")
            .bindBlocking(config -> JWTAuthHandler.create(jwtAuthProvider.getJwtAuth()));
    }

    /**
     * Set up the failure handler.
     *
     * @param router the router
     */
    private void setupFailureHandler(Router router) {
        router.route().failureHandler(rc -> {
            String message  = "";
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

    /**
     * Set up the body handler for file uploads.
     *
     * @param router the router
     */
    private void setupBodyHandler(Router router, ConfigDTO config) {
        router.route().handler(BodyHandler.create()
            .setUploadsDirectory(config.getUploadTempDirectory())
            .setBodyLimit(config.getMaxFileSize())
            .setDeleteUploadedFilesOnEnd(true));
    }

    /**
     * Set up the cors policy for the api.
     *
     * @return the resulting cors handler
     */
    private CorsHandler cors() {
        return CorsHandler.create()
                .addOrigin("*")
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
