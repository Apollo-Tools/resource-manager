package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link NamespaceRoute} class.
 *
 * @author matthi-g
 */
public class NamespaceRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Platform p1 = TestPlatformProvider.createPlatformFaas(1L, "lambda");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg1);
            K8sNamespace n1 = TestResourceProviderProvider.createNamespace(null, "ns1", r1);
            K8sNamespace n2 = TestResourceProviderProvider.createNamespace(null, "ns2", r1);
            K8sNamespace n3 = TestResourceProviderProvider.createNamespace(null, "ns3", r2);
            AccountNamespace an1 = TestAccountProvider.createAccountNamespace(null, accountDefault, n2);
            AccountNamespace an2 = TestAccountProvider.createAccountNamespace(null, accountDefault, n3);
            return sessionManager.persist(r1)
                .flatMap(result -> sessionManager.persist(r2))
                .flatMap(result -> sessionManager.persist(n1))
                .flatMap(result -> sessionManager.persist(n2))
                .flatMap(result -> sessionManager.persist(n3))
                .flatMap(result -> sessionManager.persist(an1))
                .flatMap(result -> sessionManager.persist(an2));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void listNamespaces(boolean isAdmin, Vertx vertx, VertxTestContext testContext) {
        String token = isAdmin ? jwtAdmin : jwtDefault;
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/k8snamespaces")
            .putHeader("Authorization", token)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200 && isAdmin) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(3);
                    assertThat(resultBody.getJsonObject(0).getLong("namespace_id")).isEqualTo(1L);
                    assertThat(resultBody.getJsonObject(0).getJsonObject("resource")
                        .getLong("resource_id")).isEqualTo(1L);
                    assertThat(resultBody.getJsonObject(1).getLong("namespace_id")).isEqualTo(2L);
                    assertThat(resultBody.getJsonObject(1).getJsonObject("resource")
                        .getLong("resource_id")).isEqualTo(1L);
                    assertThat(resultBody.getJsonObject(2).getLong("namespace_id")).isEqualTo(3L);
                    assertThat(resultBody.getJsonObject(2).getJsonObject("resource")
                        .getLong("resource_id")).isEqualTo(2L);
                    testContext.completeNow();
                } else if (result.statusCode() == 403 && !isAdmin) {
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void listAccountNamespaces(boolean isAdmin, Vertx vertx, VertxTestContext testContext) {
        String token = isAdmin ? jwtAdmin : jwtDefault;
        WebClient client = WebClient.create(vertx);
        client.get(API_PORT, API_URL, "/api/accounts/2/k8snamespaces")
            .putHeader("Authorization", token)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 200 && isAdmin) {
                    JsonArray resultBody = result.bodyAsJsonArray();
                    assertThat(resultBody.size()).isEqualTo(2);
                    assertThat(resultBody.getJsonObject(0).getLong("namespace_id")).isEqualTo(2L);
                    assertThat(resultBody.getJsonObject(1).getLong("namespace_id")).isEqualTo(3L);
                    testContext.completeNow();
                } else if (result.statusCode() == 403 && !isAdmin) {
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
