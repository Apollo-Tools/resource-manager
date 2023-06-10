package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.repository.log.DeploymentLogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

/**
 * This is the implementation of the #ReservationLogService.
 *
 * @author matthi-g
 */
public class ReservationLogServiceImpl extends DatabaseServiceProxy<DeploymentLog> implements ReservationLogService {
    /**
     * Create an instance from the reservationLogRepository.
     *
     * @param reservationLogRepository the reservation log repository
     */
    public ReservationLogServiceImpl(DeploymentLogRepository reservationLogRepository) {
        super(reservationLogRepository, DeploymentLog.class);
    }
}
