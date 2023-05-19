package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;

import java.util.List;

public class TestFunctionProvider {
    public static FunctionResourceIds createFunctionResourceIds(long functionId, long resourceId) {
        FunctionResourceIds ids = new FunctionResourceIds();
        ids.setFunctionId(functionId);
        ids.setResourceId(resourceId);
        return ids;
    }

    public static FunctionResourceIds createFunctionResourceIds(FunctionResource functionResource) {
        return createFunctionResourceIds(functionResource.getFunction().getFunctionId(),
            functionResource.getResource().getResourceId());
    }

    public static List<FunctionResourceIds> createFunctionResourceIdsList(long r1, long r2, long r3) {
        FunctionResourceIds ids1 = createFunctionResourceIds(1L, r1);
        FunctionResourceIds ids2 = createFunctionResourceIds(1L, r2);
        FunctionResourceIds ids3 = createFunctionResourceIds(2L, r2);
        FunctionResourceIds ids4 = createFunctionResourceIds(2L, r3);
        return List.of(ids1, ids2, ids3, ids4);
    }

    public static Runtime createRuntime(long runtimeId, String name, String templatePath) {
        Runtime runtime = new Runtime();
        runtime.setRuntimeId(runtimeId);
        runtime.setName(name);
        runtime.setTemplatePath(templatePath);
        return runtime;
    }

    public static Runtime createRuntime(long runtimeId, String name) {
        return createRuntime(runtimeId, name, "");
    }

    public static Runtime createRuntime(long runtimeId) {
        return createRuntime(runtimeId, "python3.9");
    }

    public static Function createFunction(long functionId, String name, String code, Runtime runtime) {
        Function function = new Function();
        function.setFunctionId(functionId);
        function.setName(name);
        function.setRuntime(runtime);
        function.setCode(code);
        return function;
    }

    public static Function createFunction(long functionId, String name, String code, long runtimeId) {
        Runtime runtime = createRuntime(runtimeId);
        return createFunction(functionId, name, code, runtime);
    }

    public static Function createFunction(long id, String name, String code) {
        Runtime runtime = createRuntime(1L);
        return createFunction(id, name, code, runtime);
    }

    public static Function createFunction(long id) {
        return createFunction(id, "foo", "false");
    }

    public static FunctionResource createFunctionResource(long id, long resourceId) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        Function function = createFunction(22L, "func-test", "false");
        functionResource.setFunction(function);
        Resource resource = TestResourceProvider.createResource(resourceId);
        functionResource.setResource(resource);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id, long functionId, long resourceId, Region region) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        Function function = createFunction(functionId, "func-test", "false");
        functionResource.setFunction(function);
        Resource resource = TestResourceProvider.createResource(resourceId, region);
        functionResource.setResource(resource);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id) {
        return createFunctionResource(id, 33L);
    }

    public static FunctionResource createFunctionResource(long id, Function function, boolean isDeployed) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        functionResource.setFunction(function);
        Resource resource = TestResourceProvider.createResource(33L);
        functionResource.setResource(resource);
        functionResource.setIsDeployed(isDeployed);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id, Resource resource) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        Function function = createFunction(22L, "func-test", "false");
        functionResource.setFunction(function);
        functionResource.setResource(resource);
        functionResource.setIsDeployed(true);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id, Function function, Resource resource, boolean isDeployed) {
        FunctionResource functionResource = new FunctionResource();
        functionResource.setFunctionResourceId(id);
        functionResource.setFunction(function);
        functionResource.setResource(resource);
        functionResource.setIsDeployed(isDeployed);
        return functionResource;
    }

    public static FunctionResource createFunctionResource(long id, Function function, Resource resource) {
        return createFunctionResource(id, function, resource, false);
    }

    public static FunctionReservation createFunctionReservation(long id, Function function, Resource resource,
                                                                boolean isDeployed, Reservation reservation) {
        FunctionReservation functionReservation = new FunctionReservation();
        functionReservation.setResourceReservationId(id);
        functionReservation.setFunction(function);
        functionReservation.setResource(resource);
        functionReservation.setIsDeployed(isDeployed);
        functionReservation.setStatus(TestReservationProvider.createResourceReservationStatusNew());
        functionReservation.setReservation(reservation);
        return functionReservation;
    }

    public static FunctionReservation createFunctionReservation(long id, long resourceId, Reservation reservation) {
        Function function = createFunction(22L, "func-test", "false");
        Resource resource = TestResourceProvider.createResource(resourceId);
        return createFunctionReservation(id, function, resource, true, reservation);
    }

    public static FunctionReservation createFunctionReservation(long id, long functionId, long resourceId,
            Region region) {
        Function function = createFunction(functionId, "func-test", "false");
        Resource resource = TestResourceProvider.createResource(resourceId, region);
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createFunctionReservation(id, function, resource, true, reservation);
    }

    public static FunctionReservation createFunctionReservation(long id, Reservation reservation) {
        return createFunctionReservation(id, 33L, reservation);
    }

    public static FunctionReservation createFunctionReservation(long id, Function function, boolean isDeployed) {
        Resource resource = TestResourceProvider.createResource(33L);
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createFunctionReservation(id, function, resource, isDeployed, reservation);
    }

    public static FunctionReservation createFunctionReservation(long id, Resource resource) {
        Function function = createFunction(22L, "func-test", "false");
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createFunctionReservation(id, function, resource, true, reservation);
    }

    public static FunctionReservation createFunctionReservation(long id, Function function, Resource resource) {
        Reservation reservation = TestReservationProvider.createReservation(1L);
        return createFunctionReservation(id, function, resource, false, reservation);
    }
}
