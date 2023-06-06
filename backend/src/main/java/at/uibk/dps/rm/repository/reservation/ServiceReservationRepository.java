package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
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
                    "left join fetch r.platform p " +
                    "left join fetch p.resourceType " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider rp " +
                    "left join fetch rp.environment " +
                    "left join fetch sr.status " +
                    "where sr.reservation.reservationId=:reservationId", entityClass)
                .setParameter("reservationId", reservationId)
                .getResultList()
        );
    }



    /**
     * Find a service reservation by its reservation, resourceReservation, creator and deployment status.
     *
     * @param reservationId the id of the reservation
     * @param resourceReservationId the id of the resource reservation
     * @param accountId the account id of the creator
     * @return a CompletionStage that emits the resource reservation if it exists, else null
     */
    public CompletionStage<ServiceReservation> findOneByReservationStatus(
            long reservationId, long resourceReservationId, long accountId, ReservationStatusValue statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("from ServiceReservation sr " +
                    "where sr.reservation.reservationId=:reservationId and " +
                    "sr.resourceReservationId=:resourceReservationId and " +
                    "sr.reservation.createdBy.accountId=:accountId and " +
                    "sr.status.statusValue=:statusValue", entityClass)
                .setParameter("reservationId", reservationId)
                .setParameter("resourceReservationId", resourceReservationId)
                .setParameter("accountId", accountId)
                .setParameter("statusValue", statusValue.name())
                .getSingleResultOrNull()
        );
    }
}
