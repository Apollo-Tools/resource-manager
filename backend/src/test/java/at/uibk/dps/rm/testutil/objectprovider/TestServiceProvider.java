package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Service;

public class TestServiceProvider {

    public static Service createService(long id, String name) {
        Service service = new Service();
        service.setServiceId(id);
        service.setName(name);
        return service;
    }

    public static Service createService(long id) {
        return createService(id, "test:latest");
    }
}
