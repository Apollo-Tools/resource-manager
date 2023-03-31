package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class CredentialsServiceImplTest {
    private CredentialsService credentialsService;

    @Mock
    CredentialsRepository credentialsRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        credentialsService = new CredentialsServiceImpl(credentialsRepository);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long credentialsId = 1L;
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(1L);
        Credentials entity = TestAccountProvider.createCredentials(credentialsId, resourceProvider);
        CompletionStage<Credentials> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(credentialsRepository).findByIdAndFetch(credentialsId);

        credentialsService.findOne(credentialsId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("credentials_id")).isEqualTo(1L);
                assertThat(result.getJsonObject("resource_provider").getLong("provider_id"))
                    .isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long credentialsId = 1L;
        CompletionStage<Resource> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(credentialsRepository).findByIdAndFetch(credentialsId);

        credentialsService.findOne(credentialsId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByAccount(VertxTestContext testContext) {
        long accountId = 1L;
        Credentials entity1 = TestAccountProvider.createCredentials(1L, new ResourceProvider());
        Credentials entity2 = TestAccountProvider.createCredentials(2L, new ResourceProvider());
        List<Credentials> resultList = List.of(entity1, entity2);
        CompletionStage<List<Credentials>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(credentialsRepository).findAllByAccountId(accountId);

        credentialsService.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("credentials_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("credentials_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByAccountEmpty(VertxTestContext testContext) {
        long accountId = 1L;
        List<Credentials> resultList = new ArrayList<>();
        CompletionStage<List<Credentials>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(credentialsRepository).findAllByAccountId(accountId);

        credentialsService.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(0);
                testContext.completeNow();
            })));
    }
}
