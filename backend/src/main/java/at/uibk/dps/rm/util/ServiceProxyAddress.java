package at.uibk.dps.rm.util;

import lombok.experimental.UtilityClass;

/**
 * This utility class is used to get a service proxy address.
 */
@UtilityClass
public class ServiceProxyAddress {
    /**
     * Get the service proxy address and add a prefix.
     *
     * @param prefix the prefix
     * @return the service proxy address which includes the prefix
     */
    public static String getServiceProxyAddress(String prefix) {
        return prefix + "-service-address";
    }

    /**
     * Get the service proxy address and add the entity class as prefix.
     *
     * @param entityClass the entity class used for the prefix
     * @return the service proxy address which includes the entity class as prefix
     */
    public static String getServiceProxyAddress(Class<?> entityClass) {
        return entityClass.getSimpleName().toLowerCase() + "-service-address";
    }
}
