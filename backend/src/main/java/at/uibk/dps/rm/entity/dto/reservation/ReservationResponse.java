package at.uibk.dps.rm.entity.dto.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Represents one entry in the listMyReservations operation.
 *
 * @author matthi-g
 */
@Data
public class ReservationResponse {

    public long reservationId;

    public ReservationStatusValue statusValue;

    private Timestamp createdAt;
}
