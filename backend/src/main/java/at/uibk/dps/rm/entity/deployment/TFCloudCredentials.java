package at.uibk.dps.rm.entity.deployment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TFCloudCredentials {
    private String accessKey;
    private String secretAccessKey;
    private String sessionToken;
}
