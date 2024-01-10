package at.uibk.dps.rm.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents the resource_deployment entity with type function.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("function")
@Getter
@Setter
public class FunctionDeployment extends ResourceDeployment {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id")
    private Function function;

    private String directTriggerUrl = "";

    @JsonIgnore
    private String baseUrl;

    @JsonIgnore
    private String path;

    @JsonIgnore
    private Integer metricsPort;

    @JsonIgnore
    private Integer openfaasPort;
}
