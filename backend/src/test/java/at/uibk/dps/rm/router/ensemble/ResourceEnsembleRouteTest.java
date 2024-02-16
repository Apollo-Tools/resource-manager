package at.uibk.dps.rm.router.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestEnsembleProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ResourceEnsembleRoute} class.
 *
 * @author matthi-g
 */
public class ResourceEnsembleRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Ensemble e1 = TestEnsembleProvider.createEnsemble(null, 1L, "e1", true,
                List.of(1L), List.of(1L), List.of(1L), List.of(1L), List.of(1L));
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Platform p1 = TestPlatformProvider.createPlatformContainer(1L, "lambda");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            return sessionManager.persist(e1)
                .flatMap(res -> sessionManager.persist(r1));
        }).blockingSubscribe(res -> {}, throwable -> testContext.failNow(throwable.getMessage()));
    }

    @Test
    void addResourceToEnsemble(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.post(API_PORT, API_URL, "/api/ensembles/1/resources/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 201) {
                    System.out.println(result.bodyAsString());
                    JsonObject resultBody = result.bodyAsJsonObject();
                    assertThat(resultBody.getLong("ensemble_id")).isEqualTo(1L);
                    assertThat(resultBody.getLong("resource_id")).isEqualTo(1L);
                    testContext.completeNow();
                } else {
                    System.out.println(result.statusCode());
                    System.out.println(result.bodyAsString());
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow(throwable.getMessage()));
    }
}
