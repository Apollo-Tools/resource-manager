package at.uibk.dps.rm.service.monitoring.functions;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.function.InvokeFunctionDTO;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.Map;

/**
 * The interface of the service proxy for the execution of functions.
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
        // Set timeout higher than maximum function timeout
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSendTimeout(550_000);
        return new FunctionExecutionServiceVertxEBProxy(vertx, ServiceProxyAddress
            .getServiceProxyAddress("function-execution"), deliveryOptions);
    }

    /**
     * Invoke a function.
     *
     * @param triggerUrl the direct trigger url of the function
     * @param requestBody the request body
     * @param headers the headers
     * @param resultHandler receives the serialized {@link InvokeFunctionDTO} response
     */
    void invokeFunction(String triggerUrl, String requestBody, Map<String, JsonArray> headers,
        Handler<AsyncResult<JsonObject>> resultHandler);
}
