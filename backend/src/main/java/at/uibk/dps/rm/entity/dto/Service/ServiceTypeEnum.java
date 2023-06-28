package at.uibk.dps.rm.entity.dto.Service;

import at.uibk.dps.rm.entity.model.ServiceType;
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
public enum ServiceTypeEnum {
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
    public static ServiceTypeEnum fromServiceType(ServiceType serviceType) {
        return Arrays.stream(ServiceTypeEnum.values())
            .filter(value -> value.value.equals(serviceType.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown value: " + serviceType.getName()));
    }
}
