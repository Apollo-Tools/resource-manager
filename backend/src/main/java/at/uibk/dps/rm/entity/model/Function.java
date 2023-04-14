package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class Function {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long functionId;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runtime_id")
    private Runtime runtime;

    private String code;

    @Column(insertable = false, updatable = false)
    private @Setter(value = AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private @Setter(value = AccessLevel.NONE) Timestamp updatedAt;

    @JsonIgnore
    public String getFunctionDeploymentId() {
        if (runtime == null) {
            return functionId.toString();
        }
        String runtime = getRuntime().getName();
        return (getName() + "_" + runtime.replace(".", "")).toLowerCase();
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Function function = (Function) o;

        return functionId.equals(function.functionId);
    }

    @Override
    @Generated
    public int hashCode() {
        return functionId.hashCode();
    }
}
