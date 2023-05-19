package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.*;

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


    public static ServiceReservation createServiceReservation(long id, Service service, Resource resource,
                                                                boolean isDeployed) {
        ServiceReservation serviceReservation = new ServiceReservation();
        serviceReservation.setResourceReservationId(id);
        serviceReservation.setService(service);
        serviceReservation.setResource(resource);
        serviceReservation.setIsDeployed(isDeployed);
        return serviceReservation;
    }

    public static ServiceReservation createServiceReservation(long id, long resourceId) {
        Service service = createService(22L, "test:latest");
        Resource resource = TestResourceProvider.createResource(resourceId);
        return createServiceReservation(id, service, resource, true);
    }


    public static ServiceReservation createServiceReservation(long id, long serviceId, long resourceId,
                                                                Region region) {
        Service service = createService(serviceId, "test:latest");
        Resource resource = TestResourceProvider.createResource(resourceId, region);
        return createServiceReservation(id, service, resource, true);
    }

    public static ServiceReservation createServiceReservation(long id) {
        return createServiceReservation(id, 33L);
    }

    public static ServiceReservation createServiceReservation(long id, Service service, boolean isDeployed) {
        Resource resource = TestResourceProvider.createResource(33L);
        return createServiceReservation(id, service, resource, isDeployed);
    }

    public static ServiceReservation createServiceReservation(long id, Resource resource) {
        Service service = createService(22L, "test:latest");
        return createServiceReservation(id, service, resource, true);
    }

    public static ServiceReservation createServiceReservation(long id, Service service, Resource resource) {
        return createServiceReservation(id, service, resource, false);
    }
}
