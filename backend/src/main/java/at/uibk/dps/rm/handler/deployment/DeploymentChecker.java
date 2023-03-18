package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.service.deployment.docker.DockerImageService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.util.DeploymentPath;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.Vertx;

public class DeploymentChecker {

    private final DeploymentService deploymentService;

    public DeploymentChecker(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public Completable deployResources(DeployResourcesRequest request) {
        return deploymentService.packageFunctionsCode(request)
            .flatMap(functionsToDeploy -> {
                DockerImageService dockerImageService = new DockerImageService(Vertx.currentContext().owner(),
                    request.getDockerCredentials(), functionsToDeploy.getFunctionIdentifiers(),
                    new DeploymentPath(request.getReservationId()).getFunctionsFolder());
                return dockerImageService.buildAndPushDockerImages(functionsToDeploy.getFunctionsString().toString());
            })
            .ignoreElement();
        //return deploymentService.deploy(request).ignoreElement();
    }
}
