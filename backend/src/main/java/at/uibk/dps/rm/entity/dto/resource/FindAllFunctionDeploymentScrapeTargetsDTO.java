package at.uibk.dps.rm.entity.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class FindAllFunctionDeploymentScrapeTargetsDTO {

    private long resourceDeploymentId;

    private long deploymentId;

    private long resourceId;

    private String baseUrl;

    private int metricsPort;

    @Generated
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        FindAllFunctionDeploymentScrapeTargetsDTO that = (FindAllFunctionDeploymentScrapeTargetsDTO) object;

        if (deploymentId != that.deploymentId) {
            return false;
        }
        if (resourceId != that.resourceId) {
            return false;
        }
        return Objects.equals(baseUrl, that.baseUrl);
    }

    @Generated
    @Override
    public int hashCode() {
        int result = (int) (deploymentId ^ (deploymentId >>> 32));
        result = 31 * result + (int) (resourceId ^ (resourceId >>> 32));
        result = 31 * result + (baseUrl != null ? baseUrl.hashCode() : 0);
        return result;
    }
}
