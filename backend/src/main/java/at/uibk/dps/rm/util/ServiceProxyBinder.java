package at.uibk.dps.rm.util;

import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxy;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ServiceProxyBinder {

    private final ServiceBinder serviceBinder;

    @SuppressWarnings("unchecked")
    public <S extends ServiceInterface, T extends ServiceProxy & ServiceInterface> void
    bind(Class<S> serviceClass, T service) {
        serviceBinder
            .setAddress(service.getServiceProxyAddress())
            .register(serviceClass,  (S) service);
    }

}
