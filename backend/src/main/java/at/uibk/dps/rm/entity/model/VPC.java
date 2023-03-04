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
public class VPC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vpcId;

    private String vpcIdValue;

    private String subnetIdValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(insertable = false, updatable = false)
    private @Setter(value = AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VPC vpc = (VPC) o;

        return vpcId.equals(vpc.vpcId);
    }

    @Override
    @Generated
    public int hashCode() {
        return vpcId.hashCode();
    }
}
