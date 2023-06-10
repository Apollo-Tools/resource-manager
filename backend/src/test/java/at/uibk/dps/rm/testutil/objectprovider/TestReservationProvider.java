package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Utility class to instantiate objects that are linked to the reservation entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestReservationProvider {
    public static ResourceDeployment createResourceReservation(long id, Deployment reservation, Resource resource,
                                                                ResourceDeploymentStatus resourceReservationStatus) {
        ResourceDeployment resourceReservation = new FunctionDeployment();
        resourceReservation.setResourceDeploymentId(id);
        resourceReservation.setResource(resource);
        resourceReservation.setDeployment(reservation);
        resourceReservation.setStatus(resourceReservationStatus);
        return resourceReservation;
    }

    public static Deployment createReservation(long id, boolean isActive, Account account) {
        Deployment reservation = new Deployment();
        reservation.setDeploymentId(id);
        reservation.setIsActive(isActive);
        reservation.setCreatedBy(account);
        return  reservation;
    }

    public static Deployment createReservation(long id) {
        Account account = TestAccountProvider.createAccount(1L);
        return createReservation(id, true, account);
    }

    public static ResourceDeploymentStatus createResourceReservationStatus(long id, DeploymentStatusValue status) {
        ResourceDeploymentStatus rrs = new ResourceDeploymentStatus();
        rrs.setStatusId(id);
        rrs.setStatusValue(status.name());
        return rrs;
    }

    public static ResourceDeploymentStatus createResourceReservationStatusNew() {
        return createResourceReservationStatus(1L, DeploymentStatusValue.NEW);
    }

    public static ResourceDeploymentStatus createResourceReservationStatusError() {
        return createResourceReservationStatus(2L, DeploymentStatusValue.ERROR);
    }

    public static ResourceDeploymentStatus createResourceReservationStatusDeployed() {
        return createResourceReservationStatus(3L, DeploymentStatusValue.DEPLOYED);
    }

    public static ResourceDeploymentStatus createResourceReservationStatusTerminating() {
        return createResourceReservationStatus(4L, DeploymentStatusValue.TERMINATING);
    }

    public static ResourceDeploymentStatus createResourceReservationStatusTerminated() {
        return createResourceReservationStatus(5L, DeploymentStatusValue.TERMINATED);
    }

    public static List<JsonObject> createFunctionReservationsJson(Deployment reservation) {
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionReservation(1L, reservation);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionReservation(2L, reservation);
        FunctionDeployment fr3 = TestFunctionProvider.createFunctionReservation(3L, reservation);
        return List.of(JsonObject.mapFrom(fr1), JsonObject.mapFrom(fr2),
            JsonObject.mapFrom(fr3));
    }

    public static List<JsonObject> createServiceReservationsJson(Deployment reservation) {
        ServiceDeployment sr1 = TestServiceProvider.createServiceReservation(1L, reservation);
        ServiceDeployment sr2 = TestServiceProvider.createServiceReservation(2L, reservation);
        ServiceDeployment sr3 = TestServiceProvider.createServiceReservation(3L, reservation);
        return List.of(JsonObject.mapFrom(sr1), JsonObject.mapFrom(sr2),
            JsonObject.mapFrom(sr3));
    }

    public static FunctionDeployment createFunctionReservation(long id, Deployment reservation) {
        FunctionDeployment functionReservation = new FunctionDeployment();
        functionReservation.setResourceDeploymentId(id);
        functionReservation.setFunction(TestFunctionProvider.createFunction(id));
        functionReservation.setDeployment(reservation);
        functionReservation.setStatus(createResourceReservationStatusNew());
        return functionReservation;
    }

    public static ServiceDeployment createServiceReservation(long id, Deployment reservation) {
        ServiceDeployment serviceReservation = new ServiceDeployment();
        serviceReservation.setResourceDeploymentId(id);
        serviceReservation.setService(TestServiceProvider.createService(id));
        serviceReservation.setDeployment(reservation);
        serviceReservation.setStatus(createResourceReservationStatusNew());
        return serviceReservation;
    }
}
