package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
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

    public static List<ServiceResourceIds> createServiceResourceIdsList(long resourceId) {
        ServiceResourceIds ids1 = createServiceResourceIds(1L, resourceId);
        ServiceResourceIds ids2 = createServiceResourceIds(2L, resourceId);
        return List.of(ids1, ids2);
    }

    public static K8sServiceType createServiceType(long id, String name) {
        K8sServiceType serviceType = new K8sServiceType();
        serviceType.setServiceTypeId(id);
        serviceType.setName(name);
        return serviceType;
    }

    public static K8sServiceType createServiceType(long id) {
        return createServiceType(id, "NodePort");
    }

    public static Service createService(long id, String name) {
        Service service = new Service();
        service.setServiceId(id);
        service.setName(name);
        service.setImage(name + ":latest");
        service.setK8sServiceType(createServiceType(1L));
        service.setCpu(new BigDecimal("0.1"));
        service.setMemory(1024);
        service.setPorts(List.of("80:8000"));
        service.setReplicas(1);
        service.setVolumeMounts(List.of(createVolumeMount(11L)));
        service.setEnvVars(List.of(createEnvVar(22L)));
        return service;
    }

    public static Service createService(long id) {
        return createService(id, "test");
    }


    public static ServiceDeployment createServiceDeployment(long id, Service service, Resource resource,
            boolean isDeployed, Deployment deployment, ResourceDeploymentStatus status) {
        ServiceDeployment serviceDeployment = new ServiceDeployment();
        serviceDeployment.setResourceDeploymentId(id);
        serviceDeployment.setService(service);
        serviceDeployment.setResource(resource);
        serviceDeployment.setIsDeployed(isDeployed);
        serviceDeployment.setDeployment(deployment);
        serviceDeployment.setContext("k8s-context");
        serviceDeployment.setNamespace("default");
        serviceDeployment.setStatus(status);
        return serviceDeployment;
    }

    public static ServiceDeployment createServiceDeployment(long id, Service service, Resource resource,
            boolean isDeployed, Deployment deployment) {
        ResourceDeploymentStatus status = TestDeploymentProvider.createResourceDeploymentStatusNew();
        return createServiceDeployment(id, service, resource, isDeployed, deployment, status);
    }

    public static ServiceDeployment createServiceDeployment(long id, long resourceId, Deployment deployment) {
        Service service = createService(22L, "test");
        Resource resource = TestResourceProvider.createResourceContainer(resourceId, "localhost", true);
        return createServiceDeployment(id, service, resource, true, deployment);
    }

    public static ServiceDeployment createServiceDeployment(long id, Deployment deployment) {
        return createServiceDeployment(id, 33L, deployment);
    }

    public static ServiceDeployment createServiceDeployment(long id, Resource resource, Deployment deployment) {
        Service service = createService(22L, "test");
        return createServiceDeployment(id, service, resource, false, deployment);
    }

    public static ServiceDeployment createServiceDeployment(long id, Resource resource, Deployment deployment,
            ResourceDeploymentStatus status) {
        Service service = createService(22L, "test");
        return createServiceDeployment(id, service, resource, false, deployment, status);
    }

    public static ServiceDeployment createServiceDeployment(long id, Resource resource) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        return createServiceDeployment(id, resource, deployment);
    }

    public static ServiceDeployment createServiceDeployment(long id, Service service, Resource resource) {
        Deployment deployment = TestDeploymentProvider.createDeployment(1L);
        return createServiceDeployment(id, service, resource, false, deployment);
    }

    public static VolumeMount createVolumeMount(long id) {
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setVolumeMountId(id);
        volumeMount.setName("vm");
        volumeMount.setMountPath("/build");
        volumeMount.setSizeMegabytes(BigDecimal.valueOf(100));
        return volumeMount;
    }

    public static EnvVar createEnvVar(long id) {
        EnvVar envVar = new EnvVar();
        envVar.setEnvVarId(id);
        envVar.setName("env_var");
        envVar.setValue("value");
        return envVar;
    }
}
