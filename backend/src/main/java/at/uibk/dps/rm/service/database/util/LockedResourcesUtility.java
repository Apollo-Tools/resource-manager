package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.Resource;

import at.uibk.dps.rm.exception.BadInputException;

import at.uibk.dps.rm.repository.resource.ResourceRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class that provides various methods to lock and unlock resources for deployments.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class LockedResourcesUtility {

    private final ResourceRepository resourceRepository;

    /**
     * Lock resources by a deployment.
     *
     * @param sessionManager the database session manager
     * @param lockResources the resources to lock
     * @param deployment the deployment
     * @return a Single that emits a list of all locked resources
     */
    public Single<List<Resource>> lockResources(SessionManager sessionManager, List<ResourceId> lockResources,
            Deployment deployment) {
        List<Long> lockResourcesId = lockResources.stream()
            .map(ResourceId::getResourceId)
            .collect(Collectors.toList());
        return resourceRepository.findAllByResourceIdsAndFetch(sessionManager, lockResourcesId)
            .flatMapObservable(Observable::fromIterable)
            .flatMapSingle(resource -> {
                if (!resource.getIsLockable()) {
                    return Single.error(new BadInputException("resource " + resource + " is not lockable"));
                }
                deployment.getLockedResources().add(resource);
                resource.setLockedByDeployment(deployment);
                return Single.just(resource);
            }).toList();
    }

    /**
     * Unlock deployment resources.
     *
     * @param sessionManager the database session manager
     * @param deploymentId the id of the deployment
     * @return a Completable
     */
    public Completable unlockDeploymentResources(SessionManager sessionManager, long deploymentId) {
        return resourceRepository.findAllLockedByDeploymentId(sessionManager, deploymentId)
            .flatMapObservable(Observable::fromIterable)
            .flatMapCompletable(resource -> {
                resource.setLockedByDeployment(null);
                return Completable.complete();
            });
    }
}
