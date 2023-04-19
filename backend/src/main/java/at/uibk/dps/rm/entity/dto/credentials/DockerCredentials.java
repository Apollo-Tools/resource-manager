package at.uibk.dps.rm.entity.dto.credentials;

import lombok.Data;

/**
 * Represents docker credentials that are part of the deployment.
 *
 * @author matthi-g
 */
@Data
public class DockerCredentials {
    private String username;
    private String accessToken;
}
