package at.uibk.dps.rm.entity.deployment;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class that is used in the deployment process to store cloud credentials of a single provider.
 *
 * @author matthi-g
 */
@Getter
@AllArgsConstructor
public class TFCloudCredentials {
    private String accessKey;
    private String secretAccessKey;
    private String sessionToken;
}
