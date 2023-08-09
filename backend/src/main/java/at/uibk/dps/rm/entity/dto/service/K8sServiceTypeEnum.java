package at.uibk.dps.rm.entity.dto.service;

import at.uibk.dps.rm.entity.model.K8sServiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the supported service types.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum K8sServiceTypeEnum {
    /**
     * Cluster IP
     */
    CLUSTER_IP("ClusterIP"),
    /**
     * Node Port
     */
    NODE_PORT("NodePort"),
    /**
     * Load Balancer
     */
    LOAD_BALANCER("LoadBalancer"),
    /**
     * No Service
     */
    NO_SERVICE("NoService");

    private final String value;

    /**
     * Create an instance from a ServiceType. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param serviceType the serviceType
     * @return the created object
     */
    public static K8sServiceTypeEnum fromServiceType(K8sServiceType serviceType) {
        return Arrays.stream(K8sServiceTypeEnum.values())
            .filter(value -> value.value.equals(serviceType.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown value: " + serviceType.getName()));
    }
}
