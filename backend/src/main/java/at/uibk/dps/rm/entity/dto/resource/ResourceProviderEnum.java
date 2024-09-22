package at.uibk.dps.rm.entity.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the supported resource providers.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum ResourceProviderEnum {
    /**
     * Amazon Web Services
     */
    AWS("aws"),
    /**
     * Custom Fog
     */
    CUSTOM_FOG("custom-fog"),
    /**
     * OpenFaaS
     */
    CUSTOM_EDGE("custom-edge");

    private final String value;

    /**
     * Create an instance from a string provider. This is necessary because a public method is not
     * allowed.
     * <p>
     * ref: <a href="https://stackoverflow.com/a/45082346/13164629">Source</a>
     *
     * @param provider the provider
     * @return the created object
     */
    public static ResourceProviderEnum fromString(String provider) {
        return Arrays.stream(ResourceProviderEnum.values())
            .filter(value -> value.value.equals(provider))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("unknown value: " + provider));
    }
}
