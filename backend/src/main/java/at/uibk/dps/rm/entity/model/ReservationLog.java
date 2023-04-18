package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class ReservationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id")
    private Log log;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE)
    Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ReservationLog reservationLog = (ReservationLog) obj;
        return reservationLogId.equals(reservationLog.reservationLogId);
    }

    @Override
    @Generated
    public int hashCode() {
        return reservationLogId.hashCode();
    }
}
