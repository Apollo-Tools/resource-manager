package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceServiceImplTest {

    private ServiceService service;

    @Mock
    private ServiceRepository serviceRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceServiceImpl(serviceRepository);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long serviceId = 1L;
        Service entity = TestServiceProvider.createService(1L);
        CompletionStage<Service> completionStage = CompletionStages.completedFuture(entity);

        when(serviceRepository.findByIdAndFetch(serviceId)).thenReturn(completionStage);

        service.findOne(serviceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("service_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long serviceId = 1L;
        CompletionStage<Service> completionStage = CompletionStages.completedFuture(null);

        when(serviceRepository.findByIdAndFetch(serviceId)).thenReturn(completionStage);

        service.findOne(serviceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Service s1 = TestServiceProvider.createService(1L);
        Service s2 = TestServiceProvider.createService(2L);
        CompletionStage<List<Service>> completionStage = CompletionStages.completedFuture(List.of(s1, s2));

        when(serviceRepository.findAllAndFetch()).thenReturn(completionStage);

        service.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("service_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("service_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityExistsByName(VertxTestContext testContext) {
        String serviceName = "svc";
        Service entity = TestServiceProvider.createService(1L, serviceName);
        CompletionStage<Service> completionStage = CompletionStages.completedFuture(entity);

        when(serviceRepository.findOneByName(serviceName)).thenReturn(completionStage);

        service.existsOneByName(serviceName)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityExistsByNameNotExists(VertxTestContext testContext) {
        String serviceName = "svc";
        CompletionStage<Service> completionStage = CompletionStages.completedFuture(null);

        when(serviceRepository.findOneByName(serviceName)).thenReturn(completionStage);

        service.existsOneByName(serviceName)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existsAllByIds(boolean allExist, VertxTestContext testContext) {
        Set<Long> serviceIds = Set.of(1L, 2L);
        Service s1 = TestServiceProvider.createService(1L);
        Service s2 = TestServiceProvider.createService(2L);
        List<Service> services = List.of(s1, s2);
        if (!allExist) {
            services = List.of(s2);
        }

        CompletionStage<List<Service>> completionStage = CompletionStages.completedFuture(services);
        when(serviceRepository.findAllByIds(serviceIds)).thenReturn(completionStage);

        service.existsAllByIds(serviceIds)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(allExist);
                testContext.completeNow();
            })));
    }
}
