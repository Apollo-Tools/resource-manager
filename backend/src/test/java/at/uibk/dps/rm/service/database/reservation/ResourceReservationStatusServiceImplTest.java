package at.uibk.dps.rm.service.database.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.ResourceReservationStatus;
import at.uibk.dps.rm.repository.reservation.ResourceReservationStatusRepository;
import at.uibk.dps.rm.testutil.objectprovider.TestReservationProvider;
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
public class ResourceReservationStatusServiceImplTest {

    private ResourceReservationStatusService service;

    @Mock
    private ResourceReservationStatusRepository repository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ResourceReservationStatusServiceImpl(repository);
    }

    @Test
    void findOneByStatusValue(VertxTestContext testContext) {
        String statusValue = ReservationStatusValue.NEW.name();
        ResourceReservationStatus status = TestReservationProvider.createResourceReservationStatusNew();
        CompletionStage<ResourceReservationStatus> completionStage = CompletionStages.completedFuture(status);

        when(repository.findOneByStatusValue(statusValue)).thenReturn(completionStage);

        service.findOneByStatusValue(statusValue)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getString("status_value")).isEqualTo("NEW");
                testContext.completeNow();
            })));
    }


}
