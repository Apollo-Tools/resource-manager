package at.uibk.dps.rm.entity.dto.credentials;

import lombok.Data;

import java.util.List;

/**
 * Represents credentials from the deploy resources request.
 *
 * @author matthi-g
 */
@Data
public class DeploymentCredentials {

    private List<DockerCredentials> dockerCredentials;

    private String kubeConfig = "";
}
