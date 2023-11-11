package at.uibk.dps.rm.service.monitoring.functions;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;

/**
 * The interface of the service proxy for deployment operations.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionExecutionService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionExecutionService create(WebClient webClient) {
        return new FunctionExecutionServiceImpl(webClient);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionExecutionService createProxy(Vertx vertx) {
        return new FunctionExecutionServiceVertxEBProxy(vertx, ServiceProxyAddress
            .getServiceProxyAddress("function-execution"));
    }
}
