package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.model.ServiceDeployment;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the startup and shutdown time of a service.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class ServiceStartupShutdownTime {

    private String id;

    private long deploymentId;

    @JsonProperty
    private double executionTime;

    @JsonProperty
    private List<ServiceDeployment> serviceDeployments;

    @JsonProperty
    private boolean isStartup;

    /**
     * Create an instance of the service startup/shutdown time.
     *
     * @param id the id of the service
     * @param deploymentId the deployment id
     * @param executionTime the execution time
     * @param serviceDeployments the service deployments
     * @param isStartup whether it is a startup or shutdown
     */
    @JsonCreator
    public ServiceStartupShutdownTime(@JsonProperty("id") String id,
                                      @JsonProperty("deployment_id") long deploymentId,
                                      @JsonProperty("execution_time") double executionTime,
                                      @JsonProperty("service_deployments") List<ServiceDeployment> serviceDeployments,
                                      @JsonProperty("is_startup") boolean isStartup) {
        this.id = id;
        this.deploymentId = deploymentId;
        this.executionTime = executionTime;
        this.serviceDeployments = serviceDeployments;
        this.isStartup = isStartup;
    }
}
