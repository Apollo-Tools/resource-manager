package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the resource_reservation entity.
 *
 * @author matthi-g
 */
public class ResourceReservationRepository extends Repository<ResourceReservation> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ResourceReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceReservation.class);
    }

    /**
     * Find all resource reservations by their reservation
     * @param reservationId the id of the reservation
     * @return a CompletionStage that emits a list of all resource reservations
     */
    public CompletionStage<List<ResourceReservation>> findAllByReservationId(long reservationId) {
        return sessionFactory.withSession(session ->
                session.createQuery("select distinct rr from ResourceReservation rr " +
                                "left join fetch rr.status " +
                                "where rr.reservation.reservationId=:reservationId", entityClass)
                        .setParameter("reservationId", reservationId)
                        .getResultList()
        );
    }

    /**
     * Update the trigger url of a resource reservation by its id.
     *
     * @param id the id of the resource reservation
     * @param triggerUrl the new trigger url
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> updateTriggerUrl(long id, String triggerUrl) {
        return sessionFactory.withSession(session ->
            session.createQuery("update ResourceReservation rr " +
                "set triggerUrl=:triggerUrl, isDeployed=true " +
                "where rr.resourceReservationId=:id")
                .setParameter("triggerUrl", triggerUrl)
                .setParameter("id", id)
                .executeUpdate()
        );
    }

    /**
     * Update the resource reservation status to the status value by its reservation.
     *
     * @param reservationId the id of the reservation
     * @param statusValue the new status value
     * @return a CompletionStage that emits the row count
     */
    public CompletionStage<Integer> updateReservationStatusByReservationId(long reservationId,
                                                                        ReservationStatusValue statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("update ResourceReservation rr " +
                "set status.statusId=" +
                "(select rrs.statusId from ResourceReservationStatus rrs where rrs.statusValue=:statusValue)" +
                "where rr.reservation.reservationId=:reservationId")
                .setParameter("reservationId", reservationId)
                .setParameter("statusValue", statusValue.name())
                .executeUpdate()
        );
    }

    /**
     * Create all function reservations from the functionReservations.
     *
     * @param functionReservations the list of function reservations to create
     * @return an empty CompletionStage
     */
    public CompletionStage<Void> createAllFunctionReservations(List<FunctionReservation> functionReservations) {
        return sessionFactory.withTransaction((session, tx) ->
            session.persist(functionReservations.toArray())
        );
    }

    /**
     * Create all service reservations from the serviceReservations.
     *
     * @param serviceReservations the list of service reservations to create
     * @return an empty CompletionStage
     */
    public CompletionStage<Void> createAllServiceReservations(List<ServiceReservation> serviceReservations) {
        return sessionFactory.withTransaction((session, tx) ->
            session.persist(serviceReservations.toArray())
        );
    }
}
