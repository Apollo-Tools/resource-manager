package at.uibk.dps.rm.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ServiceProxyAddress {

    public static String getServiceProxyAddress(String prefix) {
        return prefix + "-service-address";
    }

    public static String getServiceProxyAddress(Class<?> entityClass) {
        return entityClass.getSimpleName().toLowerCase() + "-service-address";
    }
}
