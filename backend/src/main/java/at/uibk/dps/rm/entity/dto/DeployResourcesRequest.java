package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.Resource;
import lombok.Data;

import java.util.List;

@Data
public class DeployResourcesRequest {

    private long reservationId;

    private List<Credentials> credentialsList;

    private List<Resource> resources;
}