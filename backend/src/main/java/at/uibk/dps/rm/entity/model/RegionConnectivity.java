package at.uibk.dps.rm.entity.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents the region connectivity entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class RegionConnectivity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Long regionConnectivityId;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp time;

    private Integer latencyMs;

    private Boolean isOnline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;
}
