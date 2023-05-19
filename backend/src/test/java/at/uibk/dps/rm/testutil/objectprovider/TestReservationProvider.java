package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class TestReservationProvider {
    public static ResourceReservation createResourceReservation(long id, FunctionResource functionResource, Reservation reservation,
                                                                ResourceReservationStatus resourceReservationStatus) {
        ResourceReservation resourceReservation = new FunctionReservation();
        resourceReservation.setResourceReservationId(id);
        resourceReservation.setFunctionResource(functionResource);
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

    public static List<JsonObject> createResourceReservationsJson(Reservation reservation) {
        FunctionResource functionResource1 = TestFunctionProvider.createFunctionResource(1L);
        FunctionResource functionResource2 = TestFunctionProvider.createFunctionResource(2L);
        FunctionResource functionResource3 = TestFunctionProvider.createFunctionResource(3L);

        ResourceReservation resourceReservation1 = createResourceReservation(1L, functionResource1,
            reservation, new ResourceReservationStatus());
        ResourceReservation resourceReservation2 = createResourceReservation(2L, functionResource2,
            reservation, new ResourceReservationStatus());
        ResourceReservation resourceReservation3 = createResourceReservation(3L, functionResource3,
            reservation, new ResourceReservationStatus());
        return List.of(JsonObject.mapFrom(resourceReservation1), JsonObject.mapFrom(resourceReservation2),
            JsonObject.mapFrom(resourceReservation3));
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
