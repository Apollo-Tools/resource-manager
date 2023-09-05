package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the service entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ServiceService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ServiceService create(Stage.SessionFactory sessionFactory) {
        return new ServiceServiceImpl(new ServiceRepository(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceService createProxy(Vertx vertx) {
        return new ServiceServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(Service.class));
    }

    /**
     * Find all services that are accessible by the account.
     *
     * @param accountId the id of the account
     * @param resultHandler receives a list of all services that are accessible by the logged-in
     *                      user.
     */
    void findAllAccessibleServices(long accountId, Handler<AsyncResult<JsonArray>> resultHandler);
}
