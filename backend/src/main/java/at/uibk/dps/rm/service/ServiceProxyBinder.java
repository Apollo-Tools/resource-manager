package at.uibk.dps.rm.service;

import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.AllArgsConstructor;

/**
 * This utility class is used to bind a new service proxy to the event bus.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class ServiceProxyBinder {

    private final ServiceBinder serviceBinder;

    /**
     * Bind a new service proxy.
     *
     * @param serviceClass the class of the service proxy
     * @param service the implementation of the service proxy
     * @param <S> the type of the service proxy
     * @param <T> the type of the implementation of the service proxy
     */
    @SuppressWarnings("unchecked")
    public <S extends ServiceInterface, T extends ServiceProxy & ServiceInterface> void
    bind(Class<S> serviceClass, T service) {
        serviceBinder
            .setAddress(service.getServiceProxyAddress())
            .register(serviceClass,  (S) service);
    }

}
