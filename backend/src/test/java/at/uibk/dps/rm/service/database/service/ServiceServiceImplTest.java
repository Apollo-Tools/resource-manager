package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.Service;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;
    
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceServiceImpl(serviceRepository, sessionFactory);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long serviceId = 1L;
        Service entity = TestServiceProvider.createService(1L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(serviceRepository.findByIdAndFetch(sessionManager, serviceId))
            .thenReturn(Maybe.just(entity));
        when(session.fetch(entity.getVolumeMounts()))
            .thenReturn(CompletionStages.completedFuture(entity.getVolumeMounts()));
        when(session.fetch(entity.getEnvVars())).thenReturn(CompletionStages.completedFuture(entity.getEnvVars()));

        service.findOne(serviceId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("service_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long serviceId = 1L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(serviceRepository.findByIdAndFetch(sessionManager, serviceId)).thenReturn(Maybe.empty());

        service.findOne(serviceId, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("Service not found");
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Service s1 = TestServiceProvider.createService(1L);
        Service s2 = TestServiceProvider.createService(2L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(serviceRepository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(s1, s2)));

        service.findAll(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("service_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("service_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
