package at.uibk.dps.rm.entity.dto.credentials;

import lombok.Data;

/**
 * Represents credentials from the deploy resources request.
 *
 * @author matthi-g
 */
@Data
public class DeploymentCredentials {
    private DockerCredentials dockerCredentials;
}
