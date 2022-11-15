package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Reservation;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class ReservationRepository extends Repository<Reservation> {
    public ReservationRepository(Stage.SessionFactory sessionFactory,
                              Class<Reservation> entityClass) {
        super(sessionFactory, entityClass);
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
}
