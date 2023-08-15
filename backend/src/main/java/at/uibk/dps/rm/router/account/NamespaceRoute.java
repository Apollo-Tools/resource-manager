package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.account.NamespaceChecker;
import at.uibk.dps.rm.handler.account.NamespaceHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.configuration.JWTAuthProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the namespace route.
 *
 * @author matthi-g
 */
public class NamespaceRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        NamespaceChecker namespaceChecker = new NamespaceChecker(serviceProxyProvider.getNamespaceService());
        NamespaceHandler namespaceHandler = new NamespaceHandler(namespaceChecker);
        ResultHandler resultHandler = new ResultHandler(namespaceHandler);

        router
            .operation("listNamespaces")
            .handler(JWTAuthProvider.getAdminAuthorizationHandler())
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("listAccountNamespaces")
            .handler(JWTAuthProvider.getAdminAuthorizationHandler())
            .handler(rc ->
                resultHandler.handleFindAllRequest(rc, namespaceHandler.getAllByAccount(rc, false)));

        router
            .operation("listMyAccountNamespaces")
            .handler(rc ->
                resultHandler.handleFindAllRequest(rc, namespaceHandler.getAllByAccount(rc, true)));
    }
}
