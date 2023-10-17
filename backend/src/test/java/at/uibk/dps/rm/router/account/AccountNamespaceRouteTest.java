package at.uibk.dps.rm.router.account;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.integration.RouterTest;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountNamespaceRouteTest extends RouterTest {

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Region reg1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
            Platform p1 = TestPlatformProvider.createPlatformFaas(1L, "lambda");
            Resource r1 = TestResourceProvider.createResource(null, "r1", p1, reg1);
            Resource r2 = TestResourceProvider.createResource(null, "r2", p1, reg1);
            K8sNamespace n1 = TestResourceProviderProvider.createNamespace(null, "ns1", r1);
            K8sNamespace n2 = TestResourceProviderProvider.createNamespace(null, "ns2", r2);
            AccountNamespace an1 = TestAccountProvider.createAccountNamespace(null, accountAdmin, n1);
            return sessionManager.persist(r1)
                .flatMap(result -> sessionManager.persist(r2))
                .flatMap(result -> sessionManager.persist(n1))
                .flatMap(result -> sessionManager.persist(n2))
                .flatMap(result -> sessionManager.persist(an1));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @Test
    void addNamespaceToAccount(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.post(API_PORT, API_URL, "/api/accounts/1/k8snamespaces/2")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 201) {
                    assertThat(result.bodyAsJsonObject().getLong("account_id")).isEqualTo(1L);
                    assertThat(result.bodyAsJsonObject().getLong("namespace_id")).isEqualTo(2L);
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void deleteNamespaceFromAccount(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.delete(API_PORT, API_URL, "/api/accounts/1/k8snamespaces/1")
            .putHeader("Authorization", jwtAdmin)
            .send()
            .subscribe(result -> {
                if (result.statusCode() == 204) {
                    testContext.completeNow();
                } else {
                    testContext.failNow("operation failed");
                }
            }, throwable -> testContext.failNow("method has thrown exception"));
    }
}
