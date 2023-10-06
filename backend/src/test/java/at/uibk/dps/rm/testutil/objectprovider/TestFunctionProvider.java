package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Utility class to instantiate objects that are linked to the function entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestFunctionProvider {
    public static FunctionResourceIds createFunctionResourceIds(long functionId, long resourceId) {
        FunctionResourceIds ids = new FunctionResourceIds();
        ids.setFunctionId(functionId);
        ids.setResourceId(resourceId);
        return ids;
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

    public static FunctionType createFunctionType(long functionTypeId, String name) {
        FunctionType functionType = new FunctionType();
        functionType.setArtifactTypeId(functionTypeId);
        functionType.setName(name);
        return functionType;
    }

    public static Function createFunction(long functionId, FunctionType functionType, String name, String code,
            Runtime runtime, boolean isFile, int timeout, int memory, Account account) {
        Function function = new Function();
        function.setFunctionId(functionId);
        function.setFunctionType(functionType);
        function.setName(name);
        function.setRuntime(runtime);
        function.setCode(code);
        function.setIsFile(isFile);
        function.setTimeoutSeconds((short) timeout);
        function.setMemoryMegabytes((short) memory);
        function.setCreatedBy(account);
        return function;
    }

    public static Function createFunction(long functionId, String name, String code, Runtime runtime, boolean isFile) {
        Account account = TestAccountProvider.createAccount(1L);
        FunctionType functionType = createFunctionType(1L, name + "type");
        return createFunction(functionId, functionType, name, code, runtime, isFile, 60,
            128, account);
    }

    public static Function createFunction(long functionId, String name, String code, long runtimeId) {
        Runtime runtime = createRuntime(runtimeId);
        return createFunction(functionId, name, code, runtime, false);
    }

    public static Function createFunction(long id, String name, String code, Runtime runtime) {
        return createFunction(id, name, code, runtime, false);
    }

    public static Function createFunction(long id, String name, String code) {
        Runtime runtime = createRuntime(1L);
        return createFunction(id, name, code, runtime, false);
    }

    public static Function createFunction(long id) {
        return createFunction(id, "foo", "false");
    }


    public static FunctionDeployment createFunctionDeployment(long id, Function function, Resource resource,
            boolean isDeployed, Deployment deployment, ResourceDeploymentStatus deploymentStatus) {
        FunctionDeployment functionDeployment = new FunctionDeployment();
        functionDeployment.setResourceDeploymentId(id);
        functionDeployment.setFunction(function);
        functionDeployment.setResource(resource);
        functionDeployment.setIsDeployed(isDeployed);
        functionDeployment.setStatus(deploymentStatus);
        functionDeployment.setDeployment(deployment);
        return functionDeployment;
    }


    public static FunctionDeployment createFunctionDeployment(long id, Function function, Resource resource,
            boolean isDeployed, Deployment deployment) {
        ResourceDeploymentStatus status = TestDeploymentProvider.createResourceDeploymentStatusNew();
        return createFunctionDeployment(id, function, resource, isDeployed, deployment, status);
    }

    public static FunctionDeployment createFunctionDeployment(long id, long resourceId, Deployment deployment) {
        Function function = createFunction(22L, "func-test", "false");
        Resource resource = TestResourceProvider.createResource(resourceId);
        return createFunctionDeployment(id, function, resource, true, deployment);
    }

    public static FunctionDeployment createFunctionDeployment(long id, Deployment deployment) {
        return createFunctionDeployment(id, 33L, deployment);
    }

    public static FunctionDeployment createFunctionDeployment(long id, Resource resource, Deployment deployment) {
        Function function = createFunction(22L, "func-test", "false");
        return createFunctionDeployment(id, function, resource, true, deployment);
    }

    public static FunctionDeployment createFunctionDeployment(long id, Resource resource, Deployment deployment,
            ResourceDeploymentStatus status) {
        Function function = createFunction(22L, "func-test", "false");
        return createFunctionDeployment(id, function, resource, true, deployment, status);
    }

    public static FunctionDeployment createFunctionDeployment(long id, Resource resource) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        return createFunctionDeployment(id, resource, deployment);
    }

    public static FunctionDeployment createFunctionDeployment(long id, Function function, Resource resource) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        return createFunctionDeployment(id, function, resource, false, deployment);
    }
}
