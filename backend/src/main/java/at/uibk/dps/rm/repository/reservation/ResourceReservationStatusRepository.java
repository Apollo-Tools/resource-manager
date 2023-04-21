package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.ResourceReservationStatus;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the resource_reservation_status entity.
 *
 * @author matthi-g
 */
public class ResourceReservationStatusRepository extends Repository<ResourceReservationStatus> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ResourceReservationStatusRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceReservationStatus.class);
    }

    /**
     * Fine a resource reservation status by its value.
     *
     * @param statusValue the value of the status
     * @return a CompletionStage that emits the resource reservation status if it exists, else null
     */
    public CompletionStage<ResourceReservationStatus> findOneByStatusValue(String statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("from ResourceReservationStatus status " +
                    "where status.statusValue=:statusValue", entityClass)
                .setParameter("statusValue", statusValue)
                .getSingleResult()
        );
    }
}
