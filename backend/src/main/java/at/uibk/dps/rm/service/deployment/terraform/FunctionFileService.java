package at.uibk.dps.rm.service.deployment.terraform;

import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.exception.RuntimeNotSupportedException;
import at.uibk.dps.rm.service.deployment.sourcecode.PackagePythonCode;
import at.uibk.dps.rm.service.deployment.sourcecode.PackageSourceCode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;

import java.nio.file.Path;
import java.util.*;

/**
 * This service prepares all functions for deployment. This includes composition and packaging of
 * the source code and composing the content of the OpenFaaS stack file.
 *
 * @author matthi-g
 */
public class FunctionFileService {

    private final Vertx vertx;

    private final FileSystem fileSystem;

    private final List<FunctionReservation> functionReservations;

    private final Path functionsDir;

    private final DockerCredentials dockerCredentials;

    private final Set<Long> functionIds = new HashSet<>();

    private FunctionsToDeploy functionsToDeploy = new FunctionsToDeploy();

    /**
     * Create an instance from vertx, functionResources, functionsDir and dockerCredentials.
     *
     * @param vertx the vertx instance
     * @param functionReservations the list of function reservations
     * @param functionsDir the directory where everything related to the functions is stored
     * @param dockerCredentials the credentials of the docker user
     */
    public FunctionFileService(Vertx vertx, List<FunctionReservation> functionReservations, Path functionsDir,
                               DockerCredentials dockerCredentials) {
        this.vertx = vertx;
        this.fileSystem = vertx.fileSystem();
        this.functionReservations = functionReservations;
        this.functionsDir = functionsDir;
        this.dockerCredentials = dockerCredentials;
    }

    /**
     * Package the source code for all function resources.
     *
     * @return a Single that emits the functions to deploy
     */
    public Single<FunctionsToDeploy> packageCode() {
        functionsToDeploy = new FunctionsToDeploy();
        PackageSourceCode packageSourceCode;
        StringBuilder functionsString = new StringBuilder();
        List<Completable> completables = new ArrayList<>();
        for (FunctionReservation fr : functionReservations) {
            Function function = fr.getFunction();
            if (functionIds.contains(function.getFunctionId())) {
                continue;
            }
            String functionIdentifier =  function.getFunctionDeploymentId();
            if (function.getRuntime().getName().startsWith("python")) {
                packageSourceCode = new PackagePythonCode(vertx, fileSystem);
                completables.add(packageSourceCode.composeSourceCode(functionsDir, functionIdentifier,
                    function.getCode()));
                if (deployFunctionOnVMOrEdge(function)) {
                    functionsString.append(String.format(
                        "  %s:\n" +
                            "    lang: python3-flask-debian\n" +
                            "    handler: ./%s\n" +
                            "    image: %s/%s:latest\n", functionIdentifier, functionIdentifier,
                        dockerCredentials.getUsername(), functionIdentifier));
                    functionsToDeploy.getDockerFunctionIdentifiers().add(functionIdentifier);
                }
            } else {
                return Single.error(RuntimeNotSupportedException::new);
            }
            functionIds.add(function.getFunctionId());
            functionsToDeploy.getFunctionIdentifiers().add(functionIdentifier);
        }
        functionsToDeploy.setDockerFunctionsString(functionsString.toString());
        // TODO: add check if this is necessary (=no changes since last push)
        if (completables.isEmpty()) {
            return Single.just(functionsToDeploy);
        }

        return Completable.merge(completables)
            .andThen(Single.fromCallable(() -> functionsToDeploy));
    }

    /**
     * Check if a function is going to be deployed on a virtual machine or edge device.
     *
     * @param function the function
     * @return true if the function has to be deployed on a vm or edge device, else false
     */
    private boolean deployFunctionOnVMOrEdge(Function function) {
        return functionReservations.stream().anyMatch(functionReservation -> {
            String resourceType = functionReservation.getResource().getPlatform().getResourceType().getResourceType();
            return functionReservation.getFunction().equals(function) &&
                (resourceType.equals("edge") || resourceType.equals("vm"));
        });
    }
}
