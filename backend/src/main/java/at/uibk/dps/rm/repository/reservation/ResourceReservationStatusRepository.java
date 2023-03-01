package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.ResourceReservationStatus;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class ResourceReservationStatusRepository extends Repository<ResourceReservationStatus> {
    public ResourceReservationStatusRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceReservationStatus.class);
    }

    public CompletionStage<ResourceReservationStatus> findOneByStatusValue(String statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("from ResourceReservationStatus status " +
                    "where status.statusValue=:statusValue", entityClass)
                .setParameter("statusValue", statusValue)
                .getSingleResult()
        );
    }
}
