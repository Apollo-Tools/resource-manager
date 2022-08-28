package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Reservation;
import org.hibernate.reactive.stage.Stage;

public class ReservationRepository extends Repository<Reservation> {
    public ReservationRepository(Stage.SessionFactory sessionFactory,
                              Class<Reservation> entityClass) {
        super(sessionFactory, entityClass);
    }
}
