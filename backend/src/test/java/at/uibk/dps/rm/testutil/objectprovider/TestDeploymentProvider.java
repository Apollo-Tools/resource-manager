package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.deployment.output.TFOutput;
import at.uibk.dps.rm.entity.deployment.output.TFOutputValue;
import at.uibk.dps.rm.entity.model.*;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

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

    public static ResourceDeployment createResourceDeployment(long id, Deployment deployment) {
        Resource resource = TestResourceProvider.createResource(1L);
        ResourceDeploymentStatus status = TestDeploymentProvider.createResourceDeploymentStatusNew();
        return createResourceDeployment(id, deployment, resource, status);
    }

    public static Deployment createDeployment(Long id, Account account) {
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(id);
        deployment.setCreatedBy(account);
        return  deployment;
    }

    public static Deployment createDeployment(Long id) {
        Account account = TestAccountProvider.createAccount(1L);
        return createDeployment(id, account);
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

    public static TFOutputValue createTFOutputValue(String fullUrl, String path, String baseUrl, int metricsPort,
            int openFaasPort) {
        TFOutputValue tfOutputValue = new TFOutputValue();
        tfOutputValue.setFullUrl(fullUrl);
        tfOutputValue.setPath(path);
        tfOutputValue.setBaseUrl(baseUrl);
        tfOutputValue.setMetricsPort(metricsPort);
        tfOutputValue.setOpenfaasPort(openFaasPort);
        return tfOutputValue;
    }

    public static DeploymentOutput createDeploymentOutput(String runtime) {
        DeploymentOutput output = new DeploymentOutput();
        TFOutput tfOutput = new TFOutput();
        Map<String, TFOutputValue> values = new HashMap<>();
        values.put("r1_foo1_" + runtime + "_1", createTFOutputValue("http://host:8080/foo1", "foo1",
            "http://host", 9100, 8080));
        values.put("r3_foo2_" + runtime + "_1", createTFOutputValue("http://host:8080/foo2", "foo2",
            "http://host", 9100, 8080));
        tfOutput.setValue(values);
        output.setResourceOutput(tfOutput);
        return output;
    }
}
