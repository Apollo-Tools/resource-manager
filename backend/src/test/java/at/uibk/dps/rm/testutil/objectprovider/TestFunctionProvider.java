package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.reservation.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;

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
                                                                boolean isDeployed) {
        FunctionReservation functionReservation = new FunctionReservation();
        functionReservation.setResourceReservationId(id);
        functionReservation.setFunction(function);
        functionReservation.setResource(resource);
        functionReservation.setIsDeployed(isDeployed);
        return functionReservation;
    }

    public static FunctionReservation createFunctionReservation(long id, long resourceId) {
        Function function = createFunction(22L, "func-test", "false");
        Resource resource = TestResourceProvider.createResource(resourceId);
        return createFunctionReservation(id, function, resource, true);
    }

    public static FunctionReservation createFunctionReservation(long id, long functionId, long resourceId,
            Region region) {
        Function function = createFunction(functionId, "func-test", "false");
        Resource resource = TestResourceProvider.createResource(resourceId, region);
        return createFunctionReservation(id, function, resource, true);
    }

    public static FunctionReservation createFunctionReservation(long id) {
        return createFunctionReservation(id, 33L);
    }

    public static FunctionReservation createFunctionReservation(long id, Function function, boolean isDeployed) {
        Resource resource = TestResourceProvider.createResource(33L);
        return createFunctionReservation(id, function, resource, isDeployed);
    }

    public static FunctionReservation createFunctionReservation(long id, Resource resource) {
        Function function = createFunction(22L, "func-test", "false");
        return createFunctionReservation(id, function, resource, true);
    }

    public static FunctionReservation createFunctionReservation(long id, Function function, Resource resource) {
        return createFunctionReservation(id, function, resource, false);
    }
}
