package at.uibk.dps.rm.service.deployment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Credentials {
    private String accessKey;
    private String secretAccessKey;
    private String sessionToken;
}
