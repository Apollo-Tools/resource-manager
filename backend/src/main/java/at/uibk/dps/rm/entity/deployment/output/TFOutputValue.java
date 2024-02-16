package at.uibk.dps.rm.entity.deployment.output;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents one entry from a {@link TFOutput} object.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class TFOutputValue {

    private String baseUrl;

    private Integer metricsPort;

    private Integer openfaasPort;

    private String path;

    private String fullUrl;
}
