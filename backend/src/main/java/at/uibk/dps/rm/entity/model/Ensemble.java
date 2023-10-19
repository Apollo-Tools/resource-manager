package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.usertypes.LongArrayType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Represents the ensemble entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
@TypeDef(name="long-array", typeClass = LongArrayType.class)
public class Ensemble {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ensembleId;

    private String name;

    @Column(name="environments", columnDefinition = "_int8")
    @Type(type="long-array")
    private List<Long> environments;

    // TODO: fix naming
    @Column(name="resource_types", columnDefinition = "_int8")
    @Type(type="long-array")
    private List<Long> resource_types;

    @Column(name="platforms", columnDefinition = "_int8")
    @Type(type="long-array")
    private List<Long> platforms;

    @Column(name="regions", columnDefinition = "_int8")
    @Type(type="long-array")
    private List<Long> regions;

    @Column(name="providers", columnDefinition = "_int8")
    @Type(type="long-array")
    private List<Long> providers;

    private Boolean isValid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Account createdBy;

    @Column(insertable = false, updatable = false)
    private  @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp updatedAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Ensemble that = (Ensemble) obj;
        return ensembleId.equals(that.ensembleId);
    }

    @Override
    @Generated
    public int hashCode() {
        return ensembleId.hashCode();
    }
}
