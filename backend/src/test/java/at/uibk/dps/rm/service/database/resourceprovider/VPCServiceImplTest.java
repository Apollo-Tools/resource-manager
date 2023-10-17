package at.uibk.dps.rm.service.database.resourceprovider;


import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link VPCServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class VPCServiceImplTest {

    private VPCService vpcService;

    @Mock
    private VPCRepository vpcRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private Account account;

    private Region r1;
    private VPC vpc1, vpc2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        vpcService = new VPCServiceImpl(vpcRepository, smProvider);
        account = TestAccountProvider.createAccount(1L);
        r1 = TestResourceProviderProvider.createRegion(1L, "us-east");
        Region r2 = TestResourceProviderProvider.createRegion(2L, "us-west");
        vpc1 = TestResourceProviderProvider.createVPC(1L, r1, account);
        vpc2 = TestResourceProviderProvider.createVPC(2L, r2, account);
    }



    @Test
    void findOneByIdAndAccountId(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(vpcRepository.findByIdAndAccountIdAndFetch(sessionManager, vpc1.getVpcId(), account.getAccountId()))
            .thenReturn(Maybe.just(vpc1));

        vpcService.findOneByIdAndAccountId(vpc1.getVpcId(), account.getAccountId(),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountIdNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(vpcRepository.findByIdAndAccountIdAndFetch(sessionManager, vpc1.getVpcId(), account.getAccountId()))
            .thenReturn(Maybe.empty());

        vpcService.findOneByIdAndAccountId(vpc1.getVpcId(), account.getAccountId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(vpcRepository.findAllByAccountIdAndFetch(sessionManager, account.getAccountId()))
            .thenReturn(Single.just(List.of(vpc1, vpc2)));

        vpcService.findAllByAccountId(account.getAccountId(),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("vpc_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(0).getJsonObject("region")).isNotNull();
                assertThat(result.getJsonObject(1).getJsonObject("region")).isNotNull();
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccount(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(vpcRepository.findByRegionIdAndAccountId(sessionManager, r1.getRegionId(), account.getAccountId()))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, account.getAccountId())).thenReturn(Maybe.just(account));
        when(sessionManager.find(Region.class, r1.getRegionId())).thenReturn(Maybe.just(r1));
        when(sessionManager.persist(vpc1)).thenReturn(Single.just(vpc1));
        vpcService.saveToAccount(account.getAccountId(), JsonObject.mapFrom(vpc1),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("created_by").getLong("account_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountRegionNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(vpcRepository.findByRegionIdAndAccountId(sessionManager, r1.getRegionId(), account.getAccountId()))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, account.getAccountId())).thenReturn(Maybe.just(account));
        when(sessionManager.find(Region.class, r1.getRegionId())).thenReturn(Maybe.empty());
        vpcService.saveToAccount(account.getAccountId(), JsonObject.mapFrom(vpc1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountAccountNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(vpcRepository.findByRegionIdAndAccountId(sessionManager, r1.getRegionId(), account.getAccountId()))
            .thenReturn(Maybe.empty());
        when(sessionManager.find(Account.class, account.getAccountId())).thenReturn(Maybe.empty());
        vpcService.saveToAccount(account.getAccountId(), JsonObject.mapFrom(vpc1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccountAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(vpcRepository.findByRegionIdAndAccountId(sessionManager, r1.getRegionId(), account.getAccountId()))
            .thenReturn(Maybe.just(vpc1));
        when(sessionManager.find(Account.class, account.getAccountId())).thenReturn(Maybe.just(account));
        vpcService.saveToAccount(account.getAccountId(), JsonObject.mapFrom(vpc1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            })));
    }
}
