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
public class ResourceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId;

    private String provider;

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
        final ResourceProvider resourceProvider = (ResourceProvider) obj;
        return providerId.equals(resourceProvider.providerId);
    }

    @Override
    @Generated
    public int hashCode() {
        return providerId.hashCode();
    }
}
