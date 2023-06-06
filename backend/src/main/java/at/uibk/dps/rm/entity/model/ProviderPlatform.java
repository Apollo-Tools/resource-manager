package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the provider_platform entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class ProviderPlatform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerPlatformId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ResourceProvider resourceProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Column(insertable = false, updatable = false)
    private  @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProviderPlatform that = (ProviderPlatform) obj;
        return providerPlatformId.equals(that.providerPlatformId);
    }

    @Override
    @Generated
    public int hashCode() {
        return providerPlatformId.hashCode();
    }
}
