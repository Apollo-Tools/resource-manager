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
    public static ResourceDeployment createResourceDeployment(Long id, Deployment deployment, Resource resource,
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

    public static Deployment createDeployment(Long id, Account account, Ensemble ensemble) {
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(id);
        deployment.setCreatedBy(account);
        deployment.setEnsemble(ensemble);
        return  deployment;
    }

    public static Deployment createDeployment(Long id, Account account) {
        return createDeployment(id, account, null);
    }

    public static Deployment createDeployment(Long id) {
        Account account = TestAccountProvider.createAccount(1L);
        return createDeployment(id, account);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatus(Long id, DeploymentStatusValue status) {
        ResourceDeploymentStatus rrs = new ResourceDeploymentStatus();
        rrs.setStatusId(id);
        rrs.setStatusValue(status.name());
        return rrs;
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusNew(Long id) {
        return createResourceDeploymentStatus(id, DeploymentStatusValue.NEW);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusNew() {
        return createResourceDeploymentStatusNew(1L);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusError() {
        return createResourceDeploymentStatus(2L, DeploymentStatusValue.ERROR);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusDeployed(Long id) {
        return createResourceDeploymentStatus(id, DeploymentStatusValue.DEPLOYED);
    }

    public static ResourceDeploymentStatus createResourceDeploymentStatusDeployed() {
        return createResourceDeploymentStatusDeployed(3L);
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
