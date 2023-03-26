package at.uibk.dps.rm.entity.dto.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class ReservationResponse {

    public long reservationId;

    public ReservationStatusValue statusValue;

    private Timestamp createdAt;
}
