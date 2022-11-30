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
public class CloudProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId;

    private String provider;

    @Column(insertable = false, updatable = false)
    private @Setter(value = AccessLevel.NONE)
    Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloudProvider cloudProvider = (CloudProvider) o;

        return providerId.equals(cloudProvider.providerId);
    }

    @Override
    @Generated
    public int hashCode() {
        return providerId.hashCode();
    }
}
