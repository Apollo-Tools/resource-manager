package at.uibk.dps.rm.router.metric;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ResourceMetricRoute} class.
 *
 * @author matthi-g
 */
public class ResourceMetricRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Platform p1 = TestPlatformProvider.createPlatformContainer(1L, "k8s");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Metric mAvailability = TestMetricProvider.createMetric(1L);
            Metric mDeploymentRole = TestMetricProvider.createMetric(20L);
            MetricValue mv1 = TestMetricProvider.createMetricValue(null, mAvailability, r1, 0.99);
            MetricValue mv2 = TestMetricProvider.createMetricValue(null, mDeploymentRole, r1,
                "labrole");
            return sessionManager.persist(r1)
                .flatMap(res -> sessionManager.persist(mv1))
                .flatMap(res -> sessionManager.persist(mv2));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void listResourceMetrics(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/resources/1/metrics")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(2);
                    assertThat(resultBody.getJsonObject(0).getLong("metric_value_id"))
                        .isEqualTo(1L);
                    assertThat(resultBody.getJsonObject(0).getJsonObject("metric").getString("metric"))
                        .isEqualTo("availability");
                    assertThat(resultBody.getJsonObject(1).getLong("metric_value_id"))
                        .isEqualTo(2L);
                    assertThat(resultBody.getJsonObject(1).getJsonObject("metric").getString("metric"))
                        .isEqualTo("deployment-role");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
