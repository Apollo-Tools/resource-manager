package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import lombok.Data;

import java.util.List;

/**
 * Represents the body of the reserveResources operation.
 *
 * @author matthi-g
 */
@Data
public class ReserveResourcesRequest {
    private List<FunctionResourceIds> functionResources;

    private List<ServiceResourceIds> serviceResources;

    private DockerCredentials dockerCredentials;

    private String kubeConfig;
}
