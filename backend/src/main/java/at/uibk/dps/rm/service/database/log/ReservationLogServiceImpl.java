package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.repository.log.ReservationLogRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

public class ReservationLogServiceImpl extends DatabaseServiceProxy<ReservationLog> implements ReservationLogService {
    public ReservationLogServiceImpl(ReservationLogRepository reservationLogRepository) {
        super(reservationLogRepository, ReservationLog.class);
    }
}
