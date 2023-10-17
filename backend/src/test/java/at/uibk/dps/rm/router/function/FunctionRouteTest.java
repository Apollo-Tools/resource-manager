package at.uibk.dps.rm.router.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link FunctionRoute} class.
 *
 * @author matthi-g
 */
public class FunctionRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Runtime rtPython = TestFunctionProvider.createRuntime(1L);
            FunctionType ft1 = TestFunctionProvider.createFunctionType(1L, "notype");
            FunctionType ft2 = TestFunctionProvider.createFunctionType(null, "ftnew");
            Function f1 = TestFunctionProvider.createFunction(null, ft1, "foo1",
                "def main():\n  print()\n", rtPython, false, 300, 1024, true, accountAdmin);
            return sessionManager.persist(ft2)
                .flatMap(res -> sessionManager.persist(f1));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void getFunction(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/functions/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("function_id")).isEqualTo(1L);
                    assertThat(resultBody.getString("name")).isEqualTo("foo1");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
