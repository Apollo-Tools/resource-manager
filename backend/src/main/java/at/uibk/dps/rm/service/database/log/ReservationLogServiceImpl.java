package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.repository.log.ReservationLogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

/**
 * This is the implementation of the #ReservationLogService.
 *
 * @author matthi-g
 */
public class ReservationLogServiceImpl extends DatabaseServiceProxy<ReservationLog> implements ReservationLogService {
    /**
     * Create an instance from the reservationLogRepository.
     *
     * @param reservationLogRepository the reservation log repository
     */
    public ReservationLogServiceImpl(ReservationLogRepository reservationLogRepository) {
        super(reservationLogRepository, ReservationLog.class);
    }
}
