package at.uibk.dps.rm.entity.dto.credentials;

import lombok.Data;

/**
 * Represents docker credentials that are part of the deployment.
 *
 * @author matthi-g
 */
@Data
public class DockerCredentials {
    private String registry = "";
    private String username;
    private String accessToken;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DockerCredentials that = (DockerCredentials) obj;

        return registry.equals(that.registry);
    }

    @Override
    public int hashCode() {
        return registry.hashCode();
    }
}
