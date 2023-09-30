package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentResponse;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DeploymentUtility {

    private final DeploymentRepositoryProvider repositoryProvider;

    public Completable composeDeploymentResponse(SessionManager sm, Deployment deployment,
                                                  List<DeploymentResponse> deploymentResponses) {
        DeploymentResponse deploymentResponse = new DeploymentResponse();
        deploymentResponse.setDeploymentId(deployment.getDeploymentId());
        deploymentResponse.setCreatedAt(deployment.getCreatedAt());
        deploymentResponses.add(deploymentResponse);
        return repositoryProvider.getResourceDeploymentRepository()
            .findAllByDeploymentIdAndFetch(sm, deployment.getDeploymentId())
            .flatMapCompletable(resourceDeployments -> Completable.fromAction(() -> {
                DeploymentStatusValue crucialDeploymentStatus = DeploymentStatusUtility
                    .checkCrucialResourceDeploymentStatus(resourceDeployments);
                deploymentResponse.setStatusValue(crucialDeploymentStatus);
            }));
    }



    /**
     * Map resource deployments to a deploy/terminate dto.
     *
     * @param sm the database session manager
     * @param deployTerminateDTO the request
     */
    public Completable mapResourceDeploymentsToDTO(SessionManager sm,
                                                    DeployTerminateDTO deployTerminateDTO) {
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
