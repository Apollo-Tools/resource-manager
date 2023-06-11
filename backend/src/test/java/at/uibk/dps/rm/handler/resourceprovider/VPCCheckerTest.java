package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link VPCChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class VPCCheckerTest {

    private VPCChecker vpcChecker;

    @Mock
    private VPCService vpcService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        vpcChecker = new VPCChecker(vpcService);
    }

    @Test
    void checkForDuplicateEntity(VertxTestContext testContext) {
        long accountId = 1L, regionId = 2L;
        JsonObject entity = new JsonObject("{\"region\": {\"region_id\": " + regionId + "}}");

        when(vpcService.existsOneByRegionIdAndAccountId(regionId, accountId))
            .thenReturn(Single.just(false));

        vpcChecker.checkForDuplicateEntity(entity, accountId)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityExists(VertxTestContext testContext) {
        long accountId = 1L, regionId = 2L;
        JsonObject entity = new JsonObject("{\"region\": {\"region_id\": " + regionId + "}}");

        when(vpcService.existsOneByRegionIdAndAccountId(regionId, accountId))
            .thenReturn(Single.just(true));

        vpcChecker.checkForDuplicateEntity(entity, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                }));
    }

    @Test
    void checkFindOneByRegionIdAndAccountId(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;
        Region region = TestResourceProviderProvider.createRegion(regionId, "aws");
        Account account = TestAccountProvider.createAccount(accountId);
        VPC vpc = TestResourceProviderProvider.createVPC(3L, region, account);

        when(vpcService.findOneByRegionIdAndAccountId(regionId, accountId))
            .thenReturn(Single.just(JsonObject.mapFrom(vpc)));

        vpcChecker.checkFindOneByRegionIdAndAccountId(regionId, accountId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("vpc_id")).isEqualTo(3L);
                    assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject("created_by").getLong("account_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindOneByRegionIdAndAccountIdNotFound(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(vpcService.findOneByRegionIdAndAccountId(regionId, accountId))
            .thenReturn(handler);

        vpcChecker.checkFindOneByRegionIdAndAccountId(regionId, accountId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkVPCForFunctionResources(VertxTestContext testContext) {
        long accountId = 1L;
        Region aws = TestResourceProviderProvider.createRegion(1L, "aws");
        Resource r1 = TestResourceProvider.createResourceEC2(1L, aws, 300.0,200.0, "t2.micro");
        Resource r2 = TestResourceProvider.createResourceEC2(2L, aws, 300.0,200.0, "t1.mini");
        Resource r3 = TestResourceProvider.createResourceOpenFaas(3L, aws, 300.0,200.0,"url", "user", "pw");
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2),
            JsonObject.mapFrom(r3)));
        JsonObject vpc = JsonObject.mapFrom(TestResourceProviderProvider.createVPC(11L, aws));

        when(vpcService.findOneByRegionIdAndAccountId(1L, accountId)).thenReturn(Single.just(vpc));

        vpcChecker.checkVPCForFunctionResources(accountId, resources)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(1);
                    assertThat(result.get(0).getLong("vpc_id")).isEqualTo(11L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkVPCForFunctionResourcesNoEc2(VertxTestContext testContext) {
        long accountId = 1L;
        Resource r1 = TestResourceProvider.createResourceOpenFaas(3L,300.0,200.0,"url", "user", "pw");
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1)));

        vpcChecker.checkVPCForFunctionResources(accountId, resources)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkVPCForFunctionResourcesOnlyContainer(VertxTestContext testContext) {
        long accountId = 1L;
        Resource r1 = TestResourceProvider.createResourceContainer(1L, "localhost", true);
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1)));

        vpcChecker.checkVPCForFunctionResources(accountId, resources)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkVPCForFunctionResourcesNotFound(VertxTestContext testContext) {
        long accountId = 1L;
        Region aws = TestResourceProviderProvider.createRegion(1L, "aws");
        Resource r1 = TestResourceProvider.createResourceEC2(1L, aws, 300.0,200.0, "t2.micro");
        JsonArray resources = new JsonArray(List.of(JsonObject.mapFrom(r1)));
        Single<JsonObject> handler = SingleHelper.getEmptySingle();

        when(vpcService.findOneByRegionIdAndAccountId(1L, accountId)).thenReturn(handler);

        vpcChecker.checkVPCForFunctionResources(accountId, resources)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
