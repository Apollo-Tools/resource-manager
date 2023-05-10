package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.DeployTerminateRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.reservation.FunctionReservationChecker;
import at.uibk.dps.rm.handler.reservation.ResourceReservationChecker;
import at.uibk.dps.rm.handler.reservation.ServiceReservationChecker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.jackson.DatabindCodec;

import java.util.List;

/**
 * Processes requests that concern deployment.
 *
 * @author matthi-g
 */
public class DeploymentHandler {

    private final DeploymentChecker deploymentChecker;

    private final CredentialsChecker credentialsChecker;

    private final FunctionReservationChecker functionReservationChecker;

    private final ServiceReservationChecker serviceReservationChecker;

    private final ResourceReservationChecker resourceReservationChecker;

    /**
     * Create an instance from the deploymentChecker, credentialsChecker,
     * functionReservationChecker, serviceReservationChecker and resourceReservationChecker.
     *
     * @param deploymentChecker the deployment checker
     * @param credentialsChecker the credentials checker
     * @param functionReservationChecker  the function reservation checker
     * @param serviceReservationChecker  the service reservation checker
     * @param resourceReservationChecker the resource reservation checker
     */
    public DeploymentHandler(DeploymentChecker deploymentChecker, CredentialsChecker credentialsChecker,
            FunctionReservationChecker functionReservationChecker, ServiceReservationChecker serviceReservationChecker,
            ResourceReservationChecker resourceReservationChecker) {
        this.deploymentChecker = deploymentChecker;
        this.credentialsChecker = credentialsChecker;
        this.functionReservationChecker = functionReservationChecker;
        this.serviceReservationChecker = serviceReservationChecker;
        this.resourceReservationChecker = resourceReservationChecker;
    }

    /**
     * Deploy all resources from the reservation that was created by the account (accountId). The
     * docker credentials must contain valid data for edge and vm deployments. The list of VPCs
     * must be non-empty for vm deployments.
     *
     * @param reservation the reservation
     * @param accountId the id of the creator of the reservation
     * @param dockerCredentials the docker credentials
     * @param vpcList the vpc list
     * @return a Completable
     */
    public Completable deployResources(Reservation reservation, long accountId, DockerCredentials dockerCredentials,
                                       List<VPC> vpcList) {
        DeployResourcesRequest request = new DeployResourcesRequest();
        request.setReservation(reservation);
        request.setDockerCredentials(dockerCredentials);
        request.setVpcList(vpcList);
        return credentialsChecker.checkFindAll(accountId)
            .flatMap(credentials -> mapCredentialsAndResourcesToRequest(request, credentials))
            .flatMap(res -> deploymentChecker.deployResources(request))
            .flatMapCompletable(tfOutput -> Completable.defer(() -> resourceReservationChecker
                .storeOutputToFunctionResources(tfOutput, request)));
    }

    /**
     * Terminate all resources from the reservation that was created by the account (accountId).
     *
     * @param reservation the reservation
     * @param accountId the id of the creator of the reservation
     * @return a Completable
     */
    public Completable terminateResources(Reservation reservation, long accountId) {
        TerminateResourcesRequest request = new TerminateResourcesRequest();
        request.setReservation(reservation);
        return credentialsChecker.checkFindAll(accountId)
            .flatMap(credentials -> mapCredentialsAndResourcesToRequest(request, credentials))
            .flatMapCompletable(res -> deploymentChecker.terminateResources(request))
            .concatWith(Completable.defer(() -> deploymentChecker.deleteTFDirs(reservation.getReservationId())));
    }

    /**
     * Map credentials and resources to a deploy/terminate request.
     *
     * @param request the request
     * @param credentials the credentials
     * @return the request with the mapped values
     * @throws JsonProcessingException if the credentials array is malformed
     */
    private Single<DeployTerminateRequest> mapCredentialsAndResourcesToRequest(DeployTerminateRequest request,
            JsonArray credentials) throws JsonProcessingException {
        ObjectMapper mapper = DatabindCodec.mapper();
        request.setCredentialsList(mapper.readValue(credentials.toString(), new TypeReference<>() {}));
        return functionReservationChecker.checkFindAllByReservationId(request.getReservation().getReservationId())
            .map(functionReservations -> {
                request.setFunctionReservations(mapper.readValue(functionReservations.toString(),
                    new TypeReference<>() {}));
                return request;
            })
            .flatMap(res -> serviceReservationChecker
                .checkFindAllByReservationId(request.getReservation().getReservationId()))
            .map(serviceReservations -> {
                request.setServiceReservations(mapper.readValue(serviceReservations.toString(),
                    new TypeReference<>() {}));
                return request;
            });
    }
}
