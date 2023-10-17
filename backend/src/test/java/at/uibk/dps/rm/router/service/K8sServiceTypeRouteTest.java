package at.uibk.dps.rm.router.service;

import at.uibk.dps.rm.testutil.integration.RouterTest;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link K8sServiceTypeRoute} class.
 *
 * @author matthi-g
 */
public class K8sServiceTypeRouteTest extends RouterTest {

    @Test
    void listK8sServiceTypes(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/k8s-service-types")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(4);
                    assertThat(resultBody.getJsonObject(0).getLong("service_type_id")).isEqualTo(1L);
                    assertThat(resultBody.getJsonObject(0).getString("name")).isEqualTo("NodePort");
                    assertThat(resultBody.getJsonObject(1).getLong("service_type_id")).isEqualTo(2L);
                    assertThat(resultBody.getJsonObject(1).getString("name"))
                        .isEqualTo("LoadBalancer");
                    assertThat(resultBody.getJsonObject(2).getLong("service_type_id")).isEqualTo(3L);
                    assertThat(resultBody.getJsonObject(2).getString("name"))
                        .isEqualTo("NoService");
                    assertThat(resultBody.getJsonObject(3).getLong("service_type_id")).isEqualTo(4L);
                    assertThat(resultBody.getJsonObject(3).getString("name"))
                        .isEqualTo("ClusterIP");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
