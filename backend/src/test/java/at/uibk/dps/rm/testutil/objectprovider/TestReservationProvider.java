package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class TestReservationProvider {
    public static ResourceReservation createResourceReservation(long id, Reservation reservation, Resource resource,
                                                                ResourceReservationStatus resourceReservationStatus) {
        ResourceReservation resourceReservation = new FunctionReservation();
        resourceReservation.setResourceReservationId(id);
        resourceReservation.setResource(resource);
        resourceReservation.setReservation(reservation);
        resourceReservation.setStatus(resourceReservationStatus);
        return resourceReservation;
    }

    public static Reservation createReservation(long id, boolean isActive, Account account) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(id);
        reservation.setIsActive(isActive);
        reservation.setCreatedBy(account);
        return  reservation;
    }

    public static Reservation createReservation(long id) {
        Account account = TestAccountProvider.createAccount(1L);
        return createReservation(id, true, account);
    }

    public static ResourceReservationStatus createResourceReservationStatus(long id, ReservationStatusValue status) {
        ResourceReservationStatus rrs = new ResourceReservationStatus();
        rrs.setStatusId(id);
        rrs.setStatusValue(status.name());
        return rrs;
    }

    public static ResourceReservationStatus createResourceReservationStatusNew() {
        return createResourceReservationStatus(1L, ReservationStatusValue.NEW);
    }

    public static ResourceReservationStatus createResourceReservationStatusError() {
        return createResourceReservationStatus(2L, ReservationStatusValue.ERROR);
    }

    public static ResourceReservationStatus createResourceReservationStatusDeployed() {
        return createResourceReservationStatus(3L, ReservationStatusValue.DEPLOYED);
    }

    public static ResourceReservationStatus createResourceReservationStatusTerminating() {
        return createResourceReservationStatus(4L, ReservationStatusValue.TERMINATING);
    }

    public static ResourceReservationStatus createResourceReservationStatusTerminated() {
        return createResourceReservationStatus(5L, ReservationStatusValue.TERMINATED);
    }

    public static List<JsonObject> createFunctionReservationsJson(Reservation reservation) {
        FunctionReservation fr1 = TestFunctionProvider.createFunctionReservation(1L, reservation);
        FunctionReservation fr2 = TestFunctionProvider.createFunctionReservation(2L, reservation);
        FunctionReservation fr3 = TestFunctionProvider.createFunctionReservation(3L, reservation);
        return List.of(JsonObject.mapFrom(fr1), JsonObject.mapFrom(fr2),
            JsonObject.mapFrom(fr3));
    }

    public static List<JsonObject> createServiceReservationsJson(Reservation reservation) {
        ServiceReservation sr1 = TestServiceProvider.createServiceReservation(1L, reservation);
        ServiceReservation sr2 = TestServiceProvider.createServiceReservation(2L, reservation);
        ServiceReservation sr3 = TestServiceProvider.createServiceReservation(3L, reservation);
        return List.of(JsonObject.mapFrom(sr1), JsonObject.mapFrom(sr2),
            JsonObject.mapFrom(sr3));
    }

    public static FunctionReservation createFunctionReservation(long id, Reservation reservation) {
        FunctionReservation functionReservation = new FunctionReservation();
        functionReservation.setResourceReservationId(id);
        functionReservation.setFunction(TestFunctionProvider.createFunction(id));
        functionReservation.setReservation(reservation);
        functionReservation.setStatus(createResourceReservationStatusNew());
        return functionReservation;
    }

    public static ServiceReservation createServiceReservation(long id, Reservation reservation) {
        ServiceReservation serviceReservation = new ServiceReservation();
        serviceReservation.setResourceReservationId(id);
        serviceReservation.setService(TestServiceProvider.createService(id));
        serviceReservation.setReservation(reservation);
        serviceReservation.setStatus(createResourceReservationStatusNew());
        return serviceReservation;
    }
}
