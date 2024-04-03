package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.model.ServiceDeployment;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceStartupShutdownTime {

    private String id;

    @JsonProperty
    private double executionTime;

    @JsonProperty
    private List<ServiceDeployment> serviceDeployments;

    @JsonProperty
    private boolean isStartup;

    @JsonCreator
    public ServiceStartupShutdownTime(@JsonProperty("id") String id,
                                      @JsonProperty("execution_time") double executionTime,
                                      @JsonProperty("service_deployments") List<ServiceDeployment> serviceDeployments,
                                      @JsonProperty("is_startup") boolean isStartup) {
        this.id = id;
        this.executionTime = executionTime;
        this.serviceDeployments = serviceDeployments;
        this.isStartup = isStartup;
    }
}
