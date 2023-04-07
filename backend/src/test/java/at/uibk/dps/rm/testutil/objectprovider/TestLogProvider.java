package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ReservationLog;

public class TestLogProvider {

    public static Log createLog(long id) {
        Log log = new Log();
        log.setLogId(id);
        log.setLogValue("log");
        return log;
    }

    public static ReservationLog createReservationLog(long id, Reservation reservation) {
        ReservationLog reservationLog = new ReservationLog();
        reservationLog.setReservationLogId(id);
        reservationLog.setReservation(reservation);
        reservationLog.setLog(createLog(id));
        return reservationLog;
    }
}
