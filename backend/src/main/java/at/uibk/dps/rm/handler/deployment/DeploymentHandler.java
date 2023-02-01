package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import io.reactivex.rxjava3.core.Completable;

public class DeploymentHandler {

    private final DeploymentChecker deploymentChecker;

    private final CredentialsChecker credentialsChecker;

    private final FunctionResourceChecker functionResourceChecker;

    public DeploymentHandler(DeploymentService deploymentService, CredentialsService credentialsService,
                             FunctionResourceService functionResourceService) {
        this.deploymentChecker = new DeploymentChecker(deploymentService);
        this.credentialsChecker = new CredentialsChecker(credentialsService);
        this.functionResourceChecker = new FunctionResourceChecker(functionResourceService);
    }

    public Completable deployResources(long reservationId, long accountId) {
        DeployResourcesRequest request = new DeployResourcesRequest();
        request.setReservationId(reservationId);
        return credentialsChecker.checkFindAll(accountId)
            .map(credentials -> {
                // TODO: add provider to resource
                request.setCredentialsList(credentials.getList());
                return request;
            })
            .flatMap(deployRequest ->
                functionResourceChecker.checkFindAllByReservationId(reservationId)
                    .map(functionResources -> {
                        deployRequest.setFunctionResources(functionResources.getList());
                        return deployRequest;
                    }
                ))
                .map(deploymentChecker::deployResources).ignoreElement();
    }
}
