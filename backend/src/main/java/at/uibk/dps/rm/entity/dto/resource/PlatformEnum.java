package at.uibk.dps.rm.entity.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the supported platforms.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum PlatformEnum {
    /**
     * AWS Lambda
     */
    LAMBDA("lambda"),
    /**
     * AWS EC2
     */
    EC2("ec2"),
    /**
     * OpenFaaS
     */
    OPENFAAS("openfaas"),
    /**
     * Kubernetes
     */
    K8S("k8s");

    private final String value;
}
