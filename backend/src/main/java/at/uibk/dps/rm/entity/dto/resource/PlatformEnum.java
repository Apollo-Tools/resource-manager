package at.uibk.dps.rm.entity.dto.resource;

import at.uibk.dps.rm.entity.model.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

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

    /**
     * Create an instance from a string platform. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param platform the platform
     * @return the created object
     */
    public static PlatformEnum fromPlatform(Platform platform) {
        return Arrays.stream(PlatformEnum.values())
            .filter(value -> value.value.equals(platform.getPlatform()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown value: " + platform));
    }
}
