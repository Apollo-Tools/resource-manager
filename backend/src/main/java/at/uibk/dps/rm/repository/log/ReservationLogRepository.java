package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

public class ReservationLogRepository extends Repository<ReservationLog> {
    public ReservationLogRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ReservationLog.class);
    }
}
