package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the resource_provider entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class ResourceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId;

    private String provider;

    @OneToMany(mappedBy = "resourceProvider")
    private Set<ProviderPlatform> providerPlatforms = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id")
    private Environment environment;

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
        ResourceProvider resourceProvider = (ResourceProvider) obj;
        return providerId.equals(resourceProvider.providerId);
    }

    @Override
    @Generated
    public int hashCode() {
        return providerId.hashCode();
    }
}
