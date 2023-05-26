package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.reservation.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;

/**
 * Utility class to instantiate objects that are linked to the service entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestServiceProvider {

    public static ServiceResourceIds createServiceResourceIds(long serviceId, long resourceId) {
        ServiceResourceIds ids = new ServiceResourceIds();
        ids.setServiceId(serviceId);
        ids.setResourceId(resourceId);
        return ids;
    }

    public static List<ServiceResourceIds> createServiceResourceIdsList(long r1) {
        ServiceResourceIds ids1 = createServiceResourceIds(1L, r1);
        ServiceResourceIds ids2 = createServiceResourceIds(2L, r1);
        return List.of(ids1, ids2);
    }

    public static ServiceType createServiceType(long id) {
        ServiceType serviceType = new ServiceType();
        serviceType.setServiceTypeId(id);
        serviceType.setName("NodePort");
        return serviceType;
    }

    public static Service createService(long id, String name) {
        Service service = new Service();
        service.setServiceId(id);
        service.setName(name);
        service.setImage(name + ":latest");
        service.setServiceType(createServiceType(1L));
        service.setCpu(new BigDecimal("0.1"));
        service.setMemory(1024);
        service.setPorts(List.of("80:8000"));
        service.setReplicas(1);
        return service;
    }

    public static Service createService(long id) {
        return createService(id, "test");
    }


    public static ServiceReservation createServiceReservation(long id, Service service, Resource resource,
            boolean isDeployed, Reservation reservation) {
        ServiceReservation serviceReservation = new ServiceReservation();
        serviceReservation.setResourceReservationId(id);
        serviceReservation.setService(service);
        serviceReservation.setResource(resource);
        serviceReservation.setIsDeployed(isDeployed);
        serviceReservation.setReservation(reservation);
        serviceReservation.setContext("k8s-context");
        serviceReservation.setNamespace("default");
        return serviceReservation;
    }

    public static ServiceReservation createServiceReservation(long id, long resourceId, Reservation reservation) {
        Service service = createService(22L, "test");
        Resource resource = TestResourceProvider.createResourceContainer(resourceId, "localhost");
        return createServiceReservation(id, service, resource, true, reservation);
    }


    public static ServiceReservation createServiceReservation(long id, long serviceId, long resourceId,
                                                                Region region) {
        Service service = createService(serviceId, "test");
        Resource resource = TestResourceProvider.createResource(resourceId, region);
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createServiceReservation(id, service, resource, true, reservation);
    }

    public static ServiceReservation createServiceReservation(long id, Reservation reservation) {
        return createServiceReservation(id, 33L, reservation);
    }

    public static ServiceReservation createServiceReservation(long id, Resource resource, Reservation reservation) {
        Service service = createService(22L, "test");
        return createServiceReservation(id, service, resource, false, reservation);
    }

    public static ServiceReservation createServiceReservation(long id, Service service, boolean isDeployed) {
        Resource resource = TestResourceProvider.createResource(33L);
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createServiceReservation(id, service, resource, isDeployed, reservation);
    }

    public static ServiceReservation createServiceReservation(long id, Resource resource) {
        Service service = createService(22L, "test");
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createServiceReservation(id, service, resource, true, reservation);
    }

    public static ServiceReservation createServiceReservation(long id, Service service, Resource resource) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createServiceReservation(id, service, resource, false, reservation);
    }
}
