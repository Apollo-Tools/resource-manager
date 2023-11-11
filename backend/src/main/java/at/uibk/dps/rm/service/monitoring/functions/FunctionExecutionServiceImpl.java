package at.uibk.dps.rm.service.monitoring.functions;

import at.uibk.dps.rm.service.ServiceProxy;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import lombok.RequiredArgsConstructor;

/**
 * This is the implementation of the #DeploymentExecutionService.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class FunctionExecutionServiceImpl extends ServiceProxy implements FunctionExecutionService {

    private final Vertx vertx = Vertx.currentContext().owner();

    private final WebClient webClient;

    @Override
    public String getServiceProxyAddress() {
        return "function-execution" + super.getServiceProxyAddress();
    }

    public void invokeFunction(String requestBody, Handler<AsyncResult<String>> resultHandler) {
        Single<String> response = webClient.postAbs(functionDeployment.getDirectTriggerUrl())
            .putHeaders(headers)
            .sendBuffer(requestBody)
            .map(response -> response.bodyAsString());
    }
}
