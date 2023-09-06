package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * A utility class that provides various methods to compose and persist resource deployments.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class SaveResourceDeploymentUtility {

    private final DeploymentRepositoryProvider repositoryProvider;

    /**
     * Persist function deployments from the deployment.
     *
     * @param sessionManager the database session manager
     * @param deployment the deployment
     * @param request the deployment request
     * @param status the status of the function deployments
     * @param resources the list of resources of the deployment
     * @return a Completable
     */
    public Completable saveFunctionDeployments(SessionManager sessionManager, Deployment deployment,
            DeployResourcesRequest request, ResourceDeploymentStatus status, List<Resource> resources) {
        return Observable.fromIterable(request.getFunctionResources())
            .map(functionResourceIds -> {
                Resource resource = resources.stream()
                    .filter(r -> r.getResourceId() == functionResourceIds.getResourceId())
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
                return createNewResourceDeployment(deployment, functionResourceIds, status, resource);
            })
            .toList()
            .flatMapCompletable(functionDeployments -> repositoryProvider.getFunctionDeploymentRepository()
                .createAll(sessionManager, functionDeployments));
    }

    /** Persist service deployments from the deployment.
     *
     * @param sessionManager the database session manager
     * @param deployment the deployment
     * @param request the deployment request
     * @param status the status of the function deployments
     * @param namespaces the namespaces to use for the deployments
     * @param resources the list of resources of the deployment
     * @return a Completable
     */
    public Completable saveServiceDeployments(SessionManager sessionManager, Deployment deployment,
            DeployResourcesRequest request, ResourceDeploymentStatus status, List<K8sNamespace> namespaces,
            List<Resource> resources) {
        if (request.getServiceResources().isEmpty()) {
            return Completable.complete();
        }
        return Observable.fromIterable(request.getServiceResources())
            .map(serviceResourceIds -> {
                Resource resource = resources.stream()
                    .filter(r -> r.getResourceId() == serviceResourceIds.getResourceId())
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
                return createNewResourceDeployment(deployment, resource, serviceResourceIds, namespaces, status);
            })
            .toList()
            .flatMapCompletable(serviceDeployments -> repositoryProvider.getServiceDeploymentRepository()
                .createAll(sessionManager, serviceDeployments));
    }

    /**
     * Create a new service deployment and set its values.
     *
     * @param deployment the deployment
     * @param ids the ids of the resource and function
     * @param status the status of the function deployment
     * @param resource the resource
     * @return the function deployment object
     */
    private FunctionDeployment createNewResourceDeployment(Deployment deployment, FunctionResourceIds ids,
                                                           ResourceDeploymentStatus status, Resource resource) {
        Function function = new Function();
        function.setFunctionId(ids.getFunctionId());
        FunctionDeployment functionDeployment = new FunctionDeployment();
        functionDeployment.setDeployment(deployment);
        functionDeployment.setResource(resource);
        functionDeployment.setFunction(function);
        functionDeployment.setStatus(status);
        return functionDeployment;
    }

    /**
     * Create a new service deployment and set its values.
     *
     * @param deployment the deployment
     * @param resource the resource
     * @param ids the ids of the resource and service
     * @param namespaces the list of available namespaces
     * @param status the status of the service deployment
     * @return the service deployment object
     */
    private ServiceDeployment createNewResourceDeployment(Deployment deployment, Resource resource,
            ServiceResourceIds ids, List<K8sNamespace> namespaces, ResourceDeploymentStatus status) {
        K8sNamespace k8sNamespace = namespaces.stream()
            .filter(namespace -> namespace.getResource().getResourceId().equals(resource.getMain().getResourceId()))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("missing namespace for resource " + resource.getName() + " (" +
                resource.getResourceId() + ")"));
        Service service = new Service();
        service.setServiceId(ids.getServiceId());
        ServiceDeployment serviceDeployment = new ServiceDeployment();
        serviceDeployment.setDeployment(deployment);
        serviceDeployment.setResource(resource);
        serviceDeployment.setService(service);
        serviceDeployment.setStatus(status);
        serviceDeployment.setNamespace(k8sNamespace.getNamespace());
        serviceDeployment.setContext("");
        return serviceDeployment;
    }
}
