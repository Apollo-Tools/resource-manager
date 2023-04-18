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
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long regionId;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_provider_id")
    private ResourceProvider resourceProvider;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;


    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Region region = (Region) obj;
        return regionId.equals(region.regionId);
    }

    @Override
    @Generated
    public int hashCode() {
        return regionId.hashCode();
    }
}
