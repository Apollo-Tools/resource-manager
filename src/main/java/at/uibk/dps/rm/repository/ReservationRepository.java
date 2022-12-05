package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Reservation;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class ReservationRepository extends Repository<Reservation> {
    public ReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Reservation.class);
    }

    public CompletionStage<Reservation> cancelReservation(long id) {
        return this.sessionFactory.withTransaction(session ->
                session.createQuery("from Reservation r " +
                                "where r.reservationId=:id", entityClass)
                        .setParameter("id", id)
                        .getSingleResultOrNull()
                        .thenApply(reservation -> {
                                if (reservation != null) {
                                    reservation.setIsActive(false);
                                }
                                return reservation;
                            })
                        );
    }

    public CompletionStage<Reservation> findByIdAndAccountId(long id, long accountId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select r from Reservation r " +
                    "left join fetch r.createdBy cb " +
                    "where r.reservationId=:id and cb.accountId=:accountId", entityClass)
                .setParameter("id", id)
                .setParameter("accountId", accountId)
                .getSingleResultOrNull()
        );
    }
}
