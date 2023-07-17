package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.dto.credentials.DeploymentCredentials;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeployTerminateDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.account.CredentialsChecker;
import at.uibk.dps.rm.handler.deployment.FunctionDeploymentChecker;
import at.uibk.dps.rm.handler.deployment.ResourceDeploymentChecker;
import at.uibk.dps.rm.handler.deployment.ServiceDeploymentChecker;
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
public class DeploymentExecutionHandler {

    private final DeploymentExecutionChecker deploymentChecker;

    private final CredentialsChecker credentialsChecker;

    private final FunctionDeploymentChecker functionDeploymentChecker;

    private final ServiceDeploymentChecker serviceDeploymentChecker;

    private final ResourceDeploymentChecker resourceDeploymentChecker;

    /**
     * Create an instance from the deploymentChecker, credentialsChecker,
     * functionDeploymentChecker, serviceDeploymentChecker and resourceDeploymentChecker.
     *
     * @param deploymentChecker the deployment checker
     * @param credentialsChecker the credentials checker
     * @param functionDeploymentChecker  the function deployment checker
     * @param serviceDeploymentChecker  the service deployment checker
     * @param resourceDeploymentChecker the resource deployment checker
     */
    public DeploymentExecutionHandler(DeploymentExecutionChecker deploymentChecker, CredentialsChecker credentialsChecker,
            FunctionDeploymentChecker functionDeploymentChecker, ServiceDeploymentChecker serviceDeploymentChecker,
            ResourceDeploymentChecker resourceDeploymentChecker) {
        this.deploymentChecker = deploymentChecker;
        this.credentialsChecker = credentialsChecker;
        this.functionDeploymentChecker = functionDeploymentChecker;
        this.serviceDeploymentChecker = serviceDeploymentChecker;
        this.resourceDeploymentChecker = resourceDeploymentChecker;
    }

    /**
     * Deploy all resources from the deployment that was created by the account (accountId). The
     * docker credentials must contain valid data for all deployments that involve OpenFaaS. The
     * list of VPCs must be non-empty for EC2 deployments.
     *
     * @param deployment the deployment
     * @param accountId the id of the creator of the deployment
     * @param deploymentCredentials the deployment credentials
     * @param vpcList the vpc list
     * @return a Completable
     */
    public Completable deployResources(Deployment deployment, long accountId,
            DeploymentCredentials deploymentCredentials, List<VPC> vpcList) {
        DeployResourcesDTO request = new DeployResourcesDTO();
        request.setDeployment(deployment);
        request.setDeploymentCredentials(deploymentCredentials);
        request.setVpcList(vpcList);
        return credentialsChecker.checkFindAll(accountId, true)
            .flatMap(cloudCredentials -> mapCredentialsAndResourcesToRequest(request, cloudCredentials))
            .flatMap(res -> deploymentChecker.applyResourceDeployment(request))
            .flatMapCompletable(tfOutput -> Completable.defer(() -> resourceDeploymentChecker
                .storeOutputToResourceDeployments(tfOutput, request)));
    }

    /**
     * Terminate all resources from the deployment that was created by the account (accountId).
     *
     * @param deployment the deployment
     * @param accountId the id of the creator of the deployment
     * @return a Completable
     */
    public Completable terminateResources(Deployment deployment, long accountId) {
        TerminateResourcesDTO request = new TerminateResourcesDTO();
        request.setDeployment(deployment);
        return credentialsChecker.checkFindAll(accountId, true)
            .flatMap(credentials -> mapCredentialsAndResourcesToRequest(request, credentials))
            .flatMapCompletable(res -> deploymentChecker.terminateResources(request))
            .concatWith(Completable.defer(() -> deploymentChecker.deleteTFDirs(deployment.getDeploymentId())));
    }

    /**
     * Map credentials and resources to a deploy/terminate request.
     *
     * @param request the request
     * @param credentials the credentials
     * @return the request with the mapped values
     * @throws JsonProcessingException if the credentials array is malformed
     */
    private Single<DeployTerminateDTO> mapCredentialsAndResourcesToRequest(DeployTerminateDTO request,
            JsonArray credentials) throws JsonProcessingException {
        ObjectMapper mapper = DatabindCodec.mapper();
        request.setCredentialsList(mapper.readValue(credentials.toString(), new TypeReference<>() {}));
        return functionDeploymentChecker.checkFindAllByDeploymentId(request.getDeployment().getDeploymentId())
            .map(functionDeployments -> {
                request.setFunctionDeployments(mapper.readValue(functionDeployments.toString(),
                    new TypeReference<>() {}));
                return request;
            })
            .flatMap(res -> serviceDeploymentChecker
                .checkFindAllByDeploymentId(request.getDeployment().getDeploymentId()))
            .map(serviceDeployments -> {
                request.setServiceDeployments(mapper.readValue(serviceDeployments.toString(),
                    new TypeReference<>() {}));
                return request;
            });
    }
}
