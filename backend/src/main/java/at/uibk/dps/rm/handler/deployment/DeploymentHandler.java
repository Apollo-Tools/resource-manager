package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Reservation;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import java.util.List;

public class DeploymentHandler {

    private final DeploymentChecker deploymentChecker;

    private final CredentialsChecker credentialsChecker;

    private final FunctionResourceChecker functionResourceChecker;

    public DeploymentHandler(ServiceProxyProvider serviceProxyProvider) {
        this.deploymentChecker = new DeploymentChecker(serviceProxyProvider.getDeploymentService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getReservationLogService(),
            serviceProxyProvider.getResourceReservationService());
        this.credentialsChecker = new CredentialsChecker(serviceProxyProvider.getCredentialsService());
        this.functionResourceChecker = new FunctionResourceChecker(serviceProxyProvider.getFunctionResourceService());
    }

    public Completable deployResources(Reservation reservation, long accountId, DockerCredentials dockerCredentials,
                                       List<VPC> vpcList) {
        DeployResourcesRequest request = new DeployResourcesRequest();
        request.setReservation(reservation);
        request.setDockerCredentials(dockerCredentials);
        request.setVpcList(vpcList);
        ObjectMapper mapper = DatabindCodec.mapper();
        return credentialsChecker.checkFindAll(accountId)
            .map(credentials -> {
                request.setCredentialsList(mapper.readValue(credentials.toString(), new TypeReference<>() {}));
                return request;
            })
            .flatMap(deployRequest ->
                functionResourceChecker.checkFindAllByReservationId(reservation.getReservationId())
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
            //TODO: add error handling (destroy everything that was created up to the error)
                .flatMapCompletable(deploymentChecker::deployResources);
    }
}
