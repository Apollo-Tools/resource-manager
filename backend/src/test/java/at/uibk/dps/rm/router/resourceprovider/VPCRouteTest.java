package at.uibk.dps.rm.router.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link VPCRoute} class.
 *
 * @author matthi-g
 */
public class VPCRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            VPC vpc1 = TestResourceProviderProvider.createVPC(null, reg1, accountAdmin);
            return sessionManager.persist(vpc1);
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void getVPC(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/vpcs/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200) {
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("vpc_id")).isEqualTo(1L);
                    assertThat(resultBody.getString("vpc_id_value")).isEqualTo("vpc-id");
                    assertThat(resultBody.getString("subnet_id_value")).isEqualTo("subnet-id");
                    assertThat(resultBody.getJsonObject("region").getString("name"))
                        .isEqualTo("us-east-1");
                    assertThat(resultBody.getJsonObject("region").getJsonObject("resource_provider")
                        .getString("provider")).isEqualTo("aws");
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
