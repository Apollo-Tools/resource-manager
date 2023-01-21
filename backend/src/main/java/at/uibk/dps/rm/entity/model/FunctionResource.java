package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class FunctionResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long functionResourceId;

    @JsonProperty("is_deployed")
    private Boolean isDeployed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id")
    private Function function;

    @Column(insertable = false, updatable = false)
    private  @Setter(value = AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionResource functionResource = (FunctionResource) o;

        return functionResourceId.equals(functionResource.functionResourceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return functionResourceId.hashCode();
    }
}
