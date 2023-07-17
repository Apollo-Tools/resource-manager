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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerCredentials that = (DockerCredentials) o;

        return registry.equals(that.registry);
    }

    @Override
    public int hashCode() {
        return registry.hashCode();
    }
}
