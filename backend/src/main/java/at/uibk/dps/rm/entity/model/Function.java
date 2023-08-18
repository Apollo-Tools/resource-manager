package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the function entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Function {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long functionId;

    private String name;

    private Short timeoutSeconds;

    private Short memoryMegabytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private FunctionType functionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runtime_id")
    private Runtime runtime;

    private String code;

    private Boolean isFile;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp updatedAt;

    /**
     * Get the function identifier, that is used for the deployment. It consists of
     * the function name and runtime.
     *
     * @return the function identifier
     */
    @JsonIgnore
    public String getFunctionDeploymentId() {
        if (runtime == null) {
            return functionId.toString();
        }
        final String runtime = getRuntime().getName();
        return (getName() + "_" + runtime.replace(".", "")).toLowerCase();
    }

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Function function = (Function) obj;
        return functionId.equals(function.functionId);
    }

    @Override
    @Generated
    public int hashCode() {
        return functionId.hashCode();
    }
}
