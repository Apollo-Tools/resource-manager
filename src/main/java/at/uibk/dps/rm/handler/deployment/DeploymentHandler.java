package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import io.reactivex.rxjava3.core.Completable;

public class DeploymentHandler {

    private final DeploymentChecker deploymentChecker;

    private final CredentialsChecker credentialsChecker;

    private final ResourceChecker resourceChecker;

    public DeploymentHandler(DeploymentService deploymentService, CredentialsService credentialsService,
                             ResourceService resourceService) {
        this.deploymentChecker = new DeploymentChecker(deploymentService);
        this.credentialsChecker = new CredentialsChecker(credentialsService);
        this.resourceChecker = new ResourceChecker(resourceService);
    }

    public Completable deployResources(long reservationId, long accountId) {
        DeployResourcesRequest request = new DeployResourcesRequest();
        request.setReservationId(reservationId);
        return credentialsChecker.checkFindAll(accountId)
            .map(credentials -> {
                request.setCredentialsList(credentials.getList());
                return request;
            })
            .flatMap(deployRequest ->
                resourceChecker.checkFindAllByReservationId(reservationId)
                    .map(resources -> {
                        deployRequest.setResources(resources.getList());
                        return deployRequest;
                    }
                ))
                .map(deploymentChecker::deployResources).ignoreElement();
    }
}
