package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.model.usertypes.BoolArrayType;
import at.uibk.dps.rm.entity.model.usertypes.ExpressionConverter;
import at.uibk.dps.rm.entity.model.usertypes.NumberArrayType;
import at.uibk.dps.rm.entity.model.usertypes.StringArrayType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
public class EnsembleSLO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ensemble_slo_id")
    private Long ensembleSLOId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ensemble_id")
    private Ensemble ensemble;

    private String name;

    @Convert(converter = ExpressionConverter.class)
    @Column(columnDefinition = "varchar")
    private ExpressionType expression;

    @Convert(converter = StringArrayType.class)
    @Column(columnDefinition = "_text")
    private List<String> valueStrings;

    @Convert(converter = NumberArrayType.class)
    @Column(columnDefinition = "_float8")
    private List<Double> valueNumbers;

    @Convert(converter = BoolArrayType.class)
    @Column(columnDefinition = "_bool")
    private List<Boolean> valueBools;

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
        EnsembleSLO that = (EnsembleSLO) obj;
        return ensembleSLOId.equals(that.ensembleSLOId);
    }

    @Override
    @Generated
    public int hashCode() {
        return ensembleSLOId.hashCode();
    }
}
