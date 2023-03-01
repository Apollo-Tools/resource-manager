package at.uibk.dps.rm.service.database.log;

import at.uibk.dps.rm.entity.model.ReservationLog;
import at.uibk.dps.rm.repository.log.ReservationLogRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class ReservationLogServiceImpl extends ServiceProxy<ReservationLog> implements ReservationLogService {
    public ReservationLogServiceImpl(ReservationLogRepository reservationLogRepository) {
        super(reservationLogRepository, ReservationLog.class);
    }
}
