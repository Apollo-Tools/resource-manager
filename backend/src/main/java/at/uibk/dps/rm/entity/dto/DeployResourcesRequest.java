package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.VPC;
import lombok.Data;

import java.util.List;

@Data
public class DeployResourcesRequest {

    private long reservationId;

    private List<Credentials> credentialsList;

    private List<FunctionResource> functionResources;

    private DockerCredentials dockerCredentials;

    private List<VPC> vpcList;
}
