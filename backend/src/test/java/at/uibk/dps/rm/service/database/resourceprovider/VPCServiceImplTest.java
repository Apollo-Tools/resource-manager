package at.uibk.dps.rm.service.database.resourceprovider;


import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class VPCServiceImplTest {

    private VPCService vpcService;

    @Mock
    private VPCRepository vpcRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        vpcService = new VPCServiceImpl(vpcRepository);
    }

    @Test
    void findOneByRegionIdAndAccountIdExists(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;
        Region region = TestResourceProviderProvider.createRegion(regionId, "aws");
        Account account = TestAccountProvider.createAccount(accountId);
        VPC vpc = TestResourceProviderProvider.createVPC(1L, region, account);
        CompletionStage<VPC> completionStage = CompletionStages.completedFuture(vpc);

        when(vpcRepository.findByRegionIdAndAccountId(regionId, accountId)).thenReturn(completionStage);

        vpcService.findOneByRegionIdAndAccountId(regionId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("vpc_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("created_by")).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByRegionIdAndAccountIdNotExists(VertxTestContext testContext) {
        long regionId = 1L, accountId = 2L;
        CompletionStage<VPC> completionStage = CompletionStages.completedFuture(null);

        when(vpcRepository.findByRegionIdAndAccountId(regionId, accountId)).thenReturn(completionStage);

        vpcService.findOneByRegionIdAndAccountId(regionId, accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }
}
