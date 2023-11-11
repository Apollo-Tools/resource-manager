package at.uibk.dps.rm.service.monitoring.functions;

import at.uibk.dps.rm.entity.dto.function.InvokeFunctionDTO;
import at.uibk.dps.rm.service.ServiceProxy;
import at.uibk.dps.rm.util.misc.MultiMapUtility;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * This is the implementation of the #DeploymentExecutionService.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class FunctionExecutionServiceImpl extends ServiceProxy implements FunctionExecutionService {

    private final WebClient webClient;

    @Override
    public String getServiceProxyAddress() {
        return "function-execution" + super.getServiceProxyAddress();
    }

    @Override
    public void invokeFunction(String triggerUrl, String requestBody, Map<String, JsonArray> headers,
            Handler<AsyncResult<JsonObject>> resultHandler) {
        MultiMap mappedHeaders = MultiMapUtility.deserializeMultimap(headers);
        Single<JsonObject> response = webClient.postAbs(triggerUrl)
            .putHeaders(mappedHeaders)
            .sendBuffer(Buffer.buffer(requestBody))
            .map(httpResponse -> {
                InvokeFunctionDTO invokeFunctionDTO = new InvokeFunctionDTO();
                invokeFunctionDTO.setStatusCode(httpResponse.getDelegate().statusCode());
                invokeFunctionDTO.setBody(httpResponse.bodyAsString());
                return JsonObject.mapFrom(invokeFunctionDTO);
            })
            .onErrorResumeNext(throwable -> {
                InvokeFunctionDTO invokeFunctionDTO = new InvokeFunctionDTO();
                invokeFunctionDTO.setStatusCode(504);
                invokeFunctionDTO.setBody(Json.encode("function did not respond"));
                return Single.just(JsonObject.mapFrom(invokeFunctionDTO));
            });
        RxVertxHandler.handleSession(response, resultHandler);
    }
}
