package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.DeployTerminateRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.function.FunctionResourceChecker;
import at.uibk.dps.rm.handler.reservation.ResourceReservationChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.jackson.DatabindCodec;

import java.util.List;

public class DeploymentHandler {

    private final DeploymentChecker deploymentChecker;

    private final CredentialsChecker credentialsChecker;

    private final FunctionResourceChecker functionResourceChecker;

    private final ResourceReservationChecker resourceReservationChecker;

    public DeploymentHandler(ServiceProxyProvider serviceProxyProvider) {
        this.deploymentChecker = new DeploymentChecker(serviceProxyProvider.getDeploymentService(),
            serviceProxyProvider.getLogService(), serviceProxyProvider.getReservationLogService());
        this.credentialsChecker = new CredentialsChecker(serviceProxyProvider.getCredentialsService());
        this.functionResourceChecker = new FunctionResourceChecker(serviceProxyProvider.getFunctionResourceService());
        this.resourceReservationChecker = new ResourceReservationChecker(serviceProxyProvider
            .getResourceReservationService());
    }

    public Completable deployResources(Reservation reservation, long accountId, DockerCredentials dockerCredentials,
                                       List<VPC> vpcList) {
        DeployResourcesRequest request = new DeployResourcesRequest();
        request.setReservation(reservation);
        request.setDockerCredentials(dockerCredentials);
        request.setVpcList(vpcList);
        return credentialsChecker.checkFindAll(accountId)
            .flatMap(credentials -> mapCredentialsAndFunctionResourcesToRequest(request, credentials))
            .flatMap(res -> deploymentChecker.deployResources(request))
            .flatMapCompletable(tfOutput -> resourceReservationChecker
                .storeOutputToFunctionResources(tfOutput, request));
    }

    public Completable terminateResources(Reservation reservation, long accountId) {
        TerminateResourcesRequest request = new TerminateResourcesRequest();
        request.setReservation(reservation);
        return credentialsChecker.checkFindAll(accountId)
            .flatMap(credentials -> mapCredentialsAndFunctionResourcesToRequest(request, credentials))
            .flatMapCompletable(res -> deploymentChecker.terminateResources(request))
            .concatWith(Completable.defer(() -> deploymentChecker.deleteTFDirs(reservation.getReservationId())));
    }

    private Single<DeployTerminateRequest> mapCredentialsAndFunctionResourcesToRequest(DeployTerminateRequest request,
                                                                                          JsonArray credentials) throws JsonProcessingException {
        ObjectMapper mapper = DatabindCodec.mapper();
        request.setCredentialsList(mapper.readValue(credentials.toString(), new TypeReference<>() {}));
        return functionResourceChecker.checkFindAllByReservationId(request.getReservation().getReservationId())
            .map(functionResources -> {
                request.setFunctionResources(mapper.readValue(functionResources.toString(),
                    new TypeReference<>() {}));
                return request;
            });
    }
}
