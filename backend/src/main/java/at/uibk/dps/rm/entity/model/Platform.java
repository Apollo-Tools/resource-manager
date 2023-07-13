package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the platform entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Platform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long platformId;

    private String platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id")
    private ResourceType resourceType;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE)
    Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Platform platform = (Platform) obj;
        return platformId.equals(platform.platformId);
    }

    @Override
    @Generated
    public int hashCode() {
        return platformId.hashCode();
    }
}
