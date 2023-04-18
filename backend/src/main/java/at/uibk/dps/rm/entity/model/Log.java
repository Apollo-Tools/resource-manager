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
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String logValue;

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
        final Log log = (Log) obj;
        return logId.equals(log.logId);
    }

    @Override
    @Generated
    public int hashCode() {
        return logId.hashCode();
    }
}
