package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.service.deployment.docker.DockerImageService;
import at.uibk.dps.rm.service.deployment.executor.TerraformExecutor;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;

public class DeploymentChecker {

    private final DeploymentService deploymentService;

    public DeploymentChecker(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public Completable deployResources(DeployResourcesRequest request) {
        DeploymentPath deploymentPath = new DeploymentPath(request.getReservationId());
        Vertx vertx = Vertx.currentContext().owner();

        return deploymentService.packageFunctionsCode(request)
            .flatMap(functionsToDeploy -> {
                DockerImageService dockerImageService = new DockerImageService(vertx, request.getDockerCredentials(),
                    functionsToDeploy.getFunctionIdentifiers(), deploymentPath.getFunctionsFolder());
                return dockerImageService.buildAndPushDockerImages(functionsToDeploy.getFunctionsString().toString());
            })
            .flatMap(res -> deploymentService.setUpTFModules(request))
            .flatMap(deploymentCredentials -> {
                TerraformExecutor terraformExecutor = new TerraformExecutor(vertx, deploymentCredentials);
                return Single.fromCallable(() -> terraformExecutor.setPluginCacheFolder(deploymentPath.getTFCacheFolder()))
                    .map(res -> terraformExecutor);
            })
            .flatMap(terraformExecutor -> terraformExecutor.init(deploymentPath.getRootFolder())
                .map(res -> terraformExecutor))
            .flatMap(terraformExecutor -> terraformExecutor.apply(deploymentPath.getRootFolder()))
            .ignoreElement();
        //return deploymentService.deploy(request).ignoreElement();
    }
}
