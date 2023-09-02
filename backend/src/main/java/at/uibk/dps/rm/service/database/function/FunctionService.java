package at.uibk.dps.rm.service.database.function;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the function entity.
 *
 * @author matthi-g
 */
@Deprecated
@ProxyGen
@VertxGen
public interface FunctionService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionService create(Stage.SessionFactory sessionFactory) {
        return new FunctionServiceImpl(new FunctionRepository(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionService createProxy(Vertx vertx) {
        return new FunctionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Function.class));
    }

    /**
     * Find all functions accessible by the account.
     *
     * @param accountId the id of the account
     * @return a Future that emits all found functions as JsonArray
     */
    Future<JsonArray> findAllAccessibleFunctions(long accountId);
}
