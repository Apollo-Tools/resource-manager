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
@SuppressWarnings("PMD")
public class VPC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vpcId;

    private String vpcIdValue;

    private String subnetIdValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Account createdBy;

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
        final VPC vpc = (VPC) obj;
        return vpcId.equals(vpc.vpcId);
    }

    @Override
    @Generated
    public int hashCode() {
        return vpcId.hashCode();
    }
}
