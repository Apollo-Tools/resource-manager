package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

/**
 * Implements database operations for the reservation_log entity.
 *
 * @author matthi-g
 */
public class ReservationLogRepository extends Repository<ReservationLog> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ReservationLogRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ReservationLog.class);
    }
}
