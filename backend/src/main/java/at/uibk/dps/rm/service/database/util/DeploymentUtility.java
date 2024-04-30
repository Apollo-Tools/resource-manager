package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentResponse;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that provides various methods to process deployment entities.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class DeploymentUtility {

    private final DeploymentRepositoryProvider repositoryProvider;

    /**
     * Get the status of a Deployment and map it to a {@link DeploymentResponse}.
     *
     * @param deployment the deployment
     * @return the composed DeploymentResponse
     */
    public Single<DeploymentResponse> composeDeploymentResponse(Deployment deployment) {
        DeploymentResponse deploymentResponse = new DeploymentResponse();
        deploymentResponse.setDeploymentId(deployment.getDeploymentId());
        deploymentResponse.setCreatedAt(deployment.getCreatedAt());
        deploymentResponse.setFinishedAt(deployment.getFinishedAt());
        return Single.just(deployment)
            .map(currDeployment -> {
                List<ResourceDeployment> resourceDeployments = new ArrayList<>();
                resourceDeployments.addAll(currDeployment.getFunctionDeployments());
                resourceDeployments.addAll(currDeployment.getServiceDeployments());
                DeploymentStatusValue crucialDeploymentStatus = DeploymentStatusUtility
                    .checkCrucialResourceDeploymentStatus(resourceDeployments);
                deploymentResponse.setStatusValue(crucialDeploymentStatus);
                return deploymentResponse;
            });
    }

    /**
     * Map resource deployments to a deploy/terminate dto.
     *
     * @param sm the database session manager
     * @param deployTerminateDTO the request
     */
    public Completable mapResourceDeploymentsToDTO(SessionManager sm, DeployTerminateDTO deployTerminateDTO) {
        long deploymentId = deployTerminateDTO.getDeployment().getDeploymentId();
        return repositoryProvider.getFunctionDeploymentRepository()
            .findAllByDeploymentId(sm, deploymentId)
            .flatMap(functionDeployments -> {
                deployTerminateDTO.setFunctionDeployments(functionDeployments);
                return repositoryProvider.getServiceDeploymentRepository().findAllByDeploymentId(sm,
                    deploymentId);
            })
            .flatMapObservable(Observable::fromIterable)
            .flatMapSingle(serviceDeployment -> sm.fetch(serviceDeployment.getService().getEnvVars())
                .flatMap(envVars -> sm.fetch(serviceDeployment.getService().getVolumeMounts()))
                .map(result -> serviceDeployment)
            )
            .toList()
            .flatMapCompletable(serviceDeployments -> {
                deployTerminateDTO.setServiceDeployments(serviceDeployments);
                return Completable.complete();
            });
    }
}
