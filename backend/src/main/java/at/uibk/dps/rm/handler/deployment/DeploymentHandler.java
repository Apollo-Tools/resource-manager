package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.service.rxjava3.database.account.CredentialsService;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.service.rxjava3.deployment.DeploymentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

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

    public Completable deployResources(long reservationId, long accountId, DockerCredentials dockerCredentials) {
        DeployResourcesRequest request = new DeployResourcesRequest();
        request.setReservationId(reservationId);
        request.setDockerCredentials(dockerCredentials);
        ObjectMapper mapper = DatabindCodec.mapper();
        return credentialsChecker.checkFindAll(accountId)
            .map(credentials -> {
                request.setCredentialsList(mapper.readValue(credentials.toString(), new TypeReference<>() {}));
                return request;
            })
            .flatMap(deployRequest ->
                functionResourceChecker.checkFindAllByReservationId(reservationId)
                    .map(functionResources -> {
                        deployRequest.setFunctionResources(mapper.readValue(functionResources.toString(),
                            new TypeReference<>() {}));
                        for (Object fr : functionResources.getList()) {
                            FunctionResource functionResource = ((JsonObject) fr).mapTo(FunctionResource.class);
                            ResourceProvider resourceProvider = functionResource.getResource().getRegion()
                                .getResourceProvider();
                            if (request.getCredentialsList()
                                .stream().noneMatch(credentials -> credentials.getResourceProvider()
                                    .getProvider()
                                    .equals(resourceProvider.getProvider()))) {
                                // TODO: set status of reservation to failed
                                break;
                            }
                        }
                        return deployRequest;
                    }
                ))
                .map(deploymentChecker::deployResources).ignoreElement();
    }
}
