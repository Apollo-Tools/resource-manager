package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the reservation entity.
 *
 * @author matthi-g
 */
public class ReservationRepository extends Repository<Reservation> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Reservation.class);
    }

    /**
     * Find all reservations created by an account.
     *
     * @param accountId the id of the account
     * @return a CompletionStage that emits a list of all reservations
     */
    public CompletionStage<List<Reservation>> findAllByAccountId(long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r from Reservation r " +
                    "where r.createdBy.accountId=:accountId " +
                    "order by r.id", entityClass)
                .setParameter("accountId", accountId)
                .getResultList()
        );
    }

    /**
     * Find a reservation by its id and the id of the creator
     *
     * @param id the id of the reservation
     * @param accountId the id of the creator account
     * @return a CompletionStage that emits the reservation if it exists, else null
     */
    public CompletionStage<Reservation> findByIdAndAccountId(long id, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select r from Reservation r " +
                    "where r.reservationId=:id and r.createdBy.accountId=:accountId", entityClass)
                .setParameter("id", id)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }
}
