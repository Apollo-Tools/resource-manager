package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UnauthorizedException;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * A utility class that provides various methods to compose and persist resource deployments.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class SaveResourceDeploymentUtility {

    private final long accountId;

    private final FunctionRepository functionRepository;

    private final ServiceRepository serviceRepository;

    /**
     * Persist function deployments from the deployment.
     *
     * @param sm the database session manager
     * @param deployment the deployment
     * @param request the deployment request
     * @param status the status of the function deployments
     * @param resources the list of resources of the deployment
     * @return a Completable
     */
    public Completable saveFunctionDeployments(SessionManager sm, Deployment deployment,
            DeployResourcesRequest request, ResourceDeploymentStatus status, List<Resource> resources) {
        if (request.getFunctionResources().isEmpty()) {
            return Completable.complete();
        }
        return Observable.fromIterable(request.getFunctionResources())
            .flatMapSingle(functionResourceIds -> {
                Resource resource = resources.stream()
                    .filter(r -> r.getResourceId() == functionResourceIds.getResourceId())
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
                return functionRepository.findByIdAndAccountId(sm, functionResourceIds.getFunctionId(), accountId, true)
                    .switchIfEmpty(Single.error(new NotFoundException(Service.class)))
                    .map(function -> createNewResourceDeployment(deployment, function, status, resource));
            })
            .toList()
            .ignoreElement();
        //  .flatMapCompletable(functionDeployments -> sm.persist(functionDeployments.toArray()));
    }

    /** Persist service deployments from the deployment.
     *
     * @param sm the database session manager
     * @param deployment the deployment
     * @param request the deployment request
     * @param status the status of the function deployments
     * @param namespaces the namespaces to use for the deployments
     * @param resources the list of resources of the deployment
     * @return a Completable
     */
    public Completable saveServiceDeployments(SessionManager sm, Deployment deployment, DeployResourcesRequest request,
            ResourceDeploymentStatus status, List<K8sNamespace> namespaces, List<Resource> resources) {
        if (request.getServiceResources().isEmpty()) {
            return Completable.complete();
        }
        return Observable.fromIterable(request.getServiceResources())
            .flatMapSingle(serviceResourceIds -> {
                Resource resource = resources.stream()
                    .filter(r -> r.getResourceId() == serviceResourceIds.getResourceId())
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
                return serviceRepository.findByIdAndAccountId(sm, serviceResourceIds.getServiceId(), accountId, true)
                    .switchIfEmpty(Single.error(new NotFoundException(Service.class)))
                    .map(service -> createNewResourceDeployment(deployment, resource, service, namespaces, status));
            })
            .ignoreElements();
    }

    /**
     * Create a new function deployment and set its values.
     *
     * @param deployment the deployment
     * @param function the function
     * @param status the status of the function deployment
     * @param resource the resource
     * @return the function deployment object
     */
    private FunctionDeployment createNewResourceDeployment(Deployment deployment, Function function,
            ResourceDeploymentStatus status, Resource resource) {
        FunctionDeployment functionDeployment = new FunctionDeployment();
        functionDeployment.setDeployment(deployment);
        functionDeployment.setResource(resource);
        functionDeployment.setFunction(function);
        functionDeployment.setStatus(status);
        deployment.getFunctionDeployments().add(functionDeployment);
        return functionDeployment;
    }

    /**
     * Create a new service deployment and set its values.
     *
     * @param deployment the deployment
     * @param resource the resource
     * @param service the service
     * @param namespaces the list of available namespaces
     * @param status the status of the service deployment
     * @return the service deployment object
     */
    private ServiceDeployment createNewResourceDeployment(Deployment deployment, Resource resource, Service service,
            List<K8sNamespace> namespaces, ResourceDeploymentStatus status) {
        K8sNamespace k8sNamespace = namespaces.stream()
            .filter(namespace -> namespace.getResource().getResourceId().equals(resource.getMain().getResourceId()))
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("missing namespace for resource " + resource.getName() + " (" +
                resource.getResourceId() + ")"));
        ServiceDeployment serviceDeployment = new ServiceDeployment();
        serviceDeployment.setDeployment(deployment);
        serviceDeployment.setResource(resource);
        serviceDeployment.setService(service);
        serviceDeployment.setStatus(status);
        serviceDeployment.setNamespace(k8sNamespace.getNamespace());
        serviceDeployment.setContext("");
        deployment.getServiceDeployments().add(serviceDeployment);
        return serviceDeployment;
    }
}
