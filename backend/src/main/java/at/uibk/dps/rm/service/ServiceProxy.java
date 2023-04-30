package at.uibk.dps.rm.service;

import io.vertx.codegen.annotations.GenIgnore;

/**
 * An abstract implementation of the {@link ServiceInterface}.
 *
 * @author matthi-g
 */
public abstract class ServiceProxy implements ServiceInterface {
    /**
     * Get the service proxy address on the event bus.
     *
     * @return the service proxy address
     */
    @GenIgnore
    public String getServiceProxyAddress() {
        return ServiceProxyAddress.getServiceProxyAddress("");
    }
}
