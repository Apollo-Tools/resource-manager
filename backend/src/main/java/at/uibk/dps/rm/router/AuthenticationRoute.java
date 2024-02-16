package at.uibk.dps.rm.router;

import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.configuration.JWTAuthProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Represents an api route that incorporates some kind of authentication.
 *
 * @author matthi-g
 */
public interface AuthenticationRoute {
    /**
     * Initialize operations of an api route.
     *
     * @param router the router of the api
     * @param serviceProxyProvider the service proxy provider
     */
    void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider, JWTAuthProvider jwtAuthProvider);
}
