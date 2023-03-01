package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class LogRepository extends Repository<Log> {
    public LogRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Log.class);
    }

    public CompletionStage<List<Log>> findAllByReservationId(long reservationId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct l from ReservationLog rl " +
                    "left join fetch rl.log l " +
                    "where rl.reservation.reservationId=:reservationId", entityClass)
                .setParameter("reservationId", reservationId)
                .getResultList()
        );
    }
}
