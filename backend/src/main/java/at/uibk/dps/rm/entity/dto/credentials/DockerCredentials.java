package at.uibk.dps.rm.entity.dto.credentials;

import lombok.Data;

@Data
public class DockerCredentials {
    private String username;
    private String accessToken;
}
