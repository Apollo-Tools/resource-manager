package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.*;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Utility class to instantiate objects that are linked to the deployment entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestDeploymentProvider {
    public static ResourceDeployment createResourceDeployment(long id, Deployment deployment, Resource resource,
                                                                ResourceDeploymentStatus resourceDeploymentStatus) {
        ResourceDeployment resourceDeployment = new FunctionDeployment();
        resourceDeployment.setResourceDeploymentId(id);
        resourceDeployment.setResource(resource);
        resourceDeployment.setDeployment(deployment);
        resourceDeployment.setStatus(resourceDeploymentStatus);
        return resourceDeployment;
    }

    public static Deployment createDeployment(long id, boolean isActive, Account account) {
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(id);
        deployment.setIsActive(isActive);
        deployment.setCreatedBy(account);
        return  deployment;
    }

    public static Deployment createDeployment(long id) {
        Account account = TestAccountProvider.createAccount(1L);
        return createDeployment(id, true, account);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatus(long id, DeploymentStatusValue status) {
        ResourceDeploymentStatus rrs = new ResourceDeploymentStatus();
        rrs.setStatusId(id);
        rrs.setStatusValue(status.name());
        return rrs;
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusNew() {
        return createResourceDeploymentStatus(1L, DeploymentStatusValue.NEW);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusError() {
        return createResourceDeploymentStatus(2L, DeploymentStatusValue.ERROR);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusDeployed() {
        return createResourceDeploymentStatus(3L, DeploymentStatusValue.DEPLOYED);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusTerminating() {
        return createResourceDeploymentStatus(4L, DeploymentStatusValue.TERMINATING);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusTerminated() {
        return createResourceDeploymentStatus(5L, DeploymentStatusValue.TERMINATED);
    }

    public static List<JsonObject> createFunctionDeploymentsJson(Deployment deployment) {
        FunctionDeployment fr1 = TestFunctionProvider.createFunctionDeployment(1L, deployment);
        FunctionDeployment fr2 = TestFunctionProvider.createFunctionDeployment(2L, deployment);
        FunctionDeployment fr3 = TestFunctionProvider.createFunctionDeployment(3L, deployment);
        return List.of(JsonObject.mapFrom(fr1), JsonObject.mapFrom(fr2),
            JsonObject.mapFrom(fr3));
    }

    public static List<JsonObject> createServiceDeploymentsJson(Deployment deployment) {
        ServiceDeployment sr1 = TestServiceProvider.createServiceDeployment(1L, deployment);
        ServiceDeployment sr2 = TestServiceProvider.createServiceDeployment(2L, deployment);
        ServiceDeployment sr3 = TestServiceProvider.createServiceDeployment(3L, deployment);
        return List.of(JsonObject.mapFrom(sr1), JsonObject.mapFrom(sr2),
            JsonObject.mapFrom(sr3));
    }
}
