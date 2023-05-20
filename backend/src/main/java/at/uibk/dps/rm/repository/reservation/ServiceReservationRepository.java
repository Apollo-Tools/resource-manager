package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the service_reservation entity.
 *
 * @author matthi-g
 */
public class ServiceReservationRepository extends Repository<ServiceReservation> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ServiceReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ServiceReservation.class);
    }

    /**
     * Find all service reservations by their reservation
     * @param reservationId the id of the reservation
     * @return a CompletionStage that emits a list of all service reservations
     */
    public CompletionStage<List<ServiceReservation>> findAllByReservationId(long reservationId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct sr from ServiceReservation sr " +
                    "left join fetch sr.service s " +
                    "left join fetch s.serviceType " +
                    "left join fetch sr.resource r " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch r.resourceType " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider " +
                    "left join fetch sr.status " +
                    "where sr.reservation.reservationId=:reservationId", entityClass)
                .setParameter("reservationId", reservationId)
                .getResultList()
        );
    }
}
