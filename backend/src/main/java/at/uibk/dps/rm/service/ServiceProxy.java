package at.uibk.dps.rm.service;

import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;

public abstract class ServiceProxy implements ServiceInterface {
    @GenIgnore
    public String getServiceProxyAddress() {
        return ServiceProxyAddress.getServiceProxyAddress("");
    }
}
