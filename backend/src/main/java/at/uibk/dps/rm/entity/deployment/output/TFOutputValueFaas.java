package at.uibk.dps.rm.entity.deployment.output;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents one entry from a {@link TFOutputFaas} object.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class TFOutputValueFaas {

    private String baseUrl;

    private Integer metricsPort;

    private Integer openfaasPort;

    private String path;

    private String fullUrl;
}
